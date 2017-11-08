package spring.demo.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by feng on 17/10/28.
 */
public class MenuDto implements Serializable {
    private static final long serialVersionUID = 6158592312145459913L;

    private Long id;

    private String url;

    private String name;

    private String parentName;

    private Long pid;

    private List<MenuDto> subMenus = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MenuDto> getSubMenus() {
        return subMenus;
    }

    public void setSubMenus(List<MenuDto> subMenus) {
        this.subMenus = subMenus;
    }

    public void addSubMenu(MenuDto subMenu) {
        this.subMenus.add(subMenu);
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public static final MenuDto of(String name, String url, String pName, Long pid) {
        MenuDto menu = new MenuDto();
        menu.setName(name);
        menu.setUrl(url);
        menu.setParentName(pName);
        menu.setPid(pid);
        return menu;
    }
}