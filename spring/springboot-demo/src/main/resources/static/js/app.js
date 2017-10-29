var mainApp = angular.module("mainApp", ["angular-loading-bar", "ui.router", "ngAnimate", "ngStorage", "ngGrid", "ui.bootstrap", "ngFileUpload"]);
mainApp.config(function ($stateProvider, $urlRouterProvider) {
    routeConfig($stateProvider, $urlRouterProvider);
});

function routeConfig($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise("/");
    $stateProvider.state("home", {url: "/", templateUrl: "./home.html"})

        .state("menu.list", {
            url: "/list",
            templateUrl: "./module/system/menu/menuList.html",
            controller: "systemMenuListCtl"
        })
        .state("menu", {url: "/menu", templateUrl: "./module/system/menu/menu.html"})
    ;
}


