package com.wfc.cxf.mq.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;

import com.wfc.cxf.mq.CodecFactory;
import com.wfc.cxf.mq.DefaultEventTemplate;
import com.wfc.cxf.mq.EventTemplate;
import com.wfc.cxf.mq.HessianCodecFactory;
import com.wfc.cxf.mq.common.EventControlConfig;
import com.wfc.cxf.mq.consumer.EventProcesser;
import com.wfc.cxf.mq.consumer.MessageAdapterHandler;
import com.wfc.cxf.mq.consumer.MessageErrorHandler;

public class DefaultEventController implements EventController {

    private CachingConnectionFactory rabbitConnectionFactory;

    private EventControlConfig config;

    private RabbitAdmin rabbitAdmin;

    private CodecFactory defaultCodecFactory = new HessianCodecFactory();

    private SimpleMessageListenerContainer msgListenerContainer; // rabbitMQ msg
                                                                 // listener
                                                                 // container

    private MessageAdapterHandler msgAdapterHandler = new MessageAdapterHandler();

    private MessageConverter serializerMessageConverter = new SerializerMessageConverter(); // 直接指定
    // queue cache, key is exchangeName
    private Map<String, DirectExchange> exchanges = new HashMap<String, DirectExchange>();
    // queue cache, key is queueName
    private Map<String, Queue> queues = new HashMap<String, Queue>();
    // bind relation of queue to exchange cache, value is exchangeName |
    // queueName
    private Set<String> binded = new HashSet<String>();

    private EventTemplate eventTemplate; // 给App使用的Event发送客户端

    private AtomicBoolean isStarted = new AtomicBoolean(false);

    private static DefaultEventController defaultEventController;

    public synchronized static DefaultEventController getInstance(EventControlConfig config) {
        if (defaultEventController == null) {
            defaultEventController = new DefaultEventController(config);
        }
        return defaultEventController;
    }

    private DefaultEventController(EventControlConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config can not be null.");
        }
        this.config = config;
        initRabbitConnectionFactory();
        // 初始化AmqpAdmin
        rabbitAdmin = new RabbitAdmin(rabbitConnectionFactory);
        // 初始化RabbitTemplate
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
        rabbitTemplate.setMessageConverter(serializerMessageConverter);
        eventTemplate = new DefaultEventTemplate(rabbitTemplate, defaultCodecFactory, this);
    }

    /**
     * 初始化rabbitmq连接
     */
    private void initRabbitConnectionFactory() {
        rabbitConnectionFactory = new CachingConnectionFactory();
        rabbitConnectionFactory.setHost(config.getServerHost());
        rabbitConnectionFactory.setChannelCacheSize(config.getEventMsgProcessNum());
        rabbitConnectionFactory.setPort(config.getPort());
        rabbitConnectionFactory.setUsername(config.getUsername());
        rabbitConnectionFactory.setPassword(config.getPassword());
        if (!StringUtils.isEmpty(config.getVirtualHost())) {
            rabbitConnectionFactory.setVirtualHost(config.getVirtualHost());
        }
    }

    /**
     * 注销程序
     */
    public synchronized void destroy() throws Exception {
        if (!isStarted.get()) {
            return;
        }
        msgListenerContainer.stop();
        eventTemplate = null;
        rabbitAdmin = null;
        rabbitConnectionFactory.destroy();
    }

    @Override
    public void start() {
        if (isStarted.get()) {
            return;
        }
        Set<String> mapping = msgAdapterHandler.getAllBinding();
        for (String relation : mapping) {
            String[] relaArr = relation.split("\\|");
            declareBinding(relaArr[1], relaArr[0]);
        }
        initMsgListenerAdapter();
        isStarted.set(true);
    }

    /**
     * 初始化消息监听器容器
     */
    private void initMsgListenerAdapter() {
        MessageListener listener = new MessageListenerAdapter(msgAdapterHandler, serializerMessageConverter);
        msgListenerContainer = new SimpleMessageListenerContainer();
        msgListenerContainer.setConnectionFactory(rabbitConnectionFactory);
        msgListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        msgListenerContainer.setMessageListener(listener);
        msgListenerContainer.setErrorHandler(new MessageErrorHandler());
        msgListenerContainer.setPrefetchCount(config.getPrefetchSize()); // 设置每个消费者消息的预取值
        msgListenerContainer.setConcurrentConsumers(config.getEventMsgProcessNum());
        msgListenerContainer.setTxSize(config.getPrefetchSize());// 设置有事务时处理的消息数
        msgListenerContainer.setQueues(queues.values().toArray(new Queue[queues.size()]));
        msgListenerContainer.start();
    }

    @Override
    public EventTemplate getEopEventTemplate() {
        return eventTemplate;
    }

    @Override
    public EventController add(String queueName, String exchangeName, EventProcesser eventProcesser) {
        return add(queueName, exchangeName, eventProcesser, defaultCodecFactory);
    }

    public EventController add(String queueName, String exchangeName, EventProcesser eventProcesser,
            CodecFactory codecFactory) {
        msgAdapterHandler.add(queueName, exchangeName, eventProcesser, defaultCodecFactory);
        if (isStarted.get()) {
            initMsgListenerAdapter();
        }
        return this;
    }

    @Override
    public EventController add(Map<String, String> bindings, EventProcesser eventProcesser) {
        return add(bindings, eventProcesser, defaultCodecFactory);
    }

    public EventController add(Map<String, String> bindings, EventProcesser eventProcesser, CodecFactory codecFactory) {
        for (Map.Entry<String, String> item : bindings.entrySet())
            msgAdapterHandler.add(item.getKey(), item.getValue(), eventProcesser, codecFactory);
        return this;
    }

    /**
     * exchange和queue是否已经绑定
     */
    protected boolean beBinded(String exchangeName, String queueName) {
        return binded.contains(exchangeName + "|" + queueName);
    }

    /**
     * 声明exchange和queue它们已经绑定关系
     */
    protected synchronized void declareBinding(String exchangeName, String queueName) {
        String bindRelation = exchangeName + "|" + queueName;
        if (binded.contains(bindRelation))
            return;

        boolean needBinding = false;
        DirectExchange directExchange = exchanges.get(exchangeName);
        if (directExchange == null) {
            directExchange = new DirectExchange(exchangeName, true, false, null);
            exchanges.put(exchangeName, directExchange);
            rabbitAdmin.declareExchange(directExchange);// 声明exchange
            needBinding = true;
        }

        Queue queue = queues.get(queueName);
        if (queue == null) {
            queue = new Queue(queueName, true, false, false);
            queues.put(queueName, queue);
            rabbitAdmin.declareQueue(queue); // 声明queue
            needBinding = true;
        }

        if (needBinding) {
            Binding binding = BindingBuilder.bind(queue).to(directExchange).with(queueName);// 将queue绑定到exchange
            rabbitAdmin.declareBinding(binding);// 声明绑定关系
            binded.add(bindRelation);
        }
    }

}
