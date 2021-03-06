var mainApp = angular.module("mainApp", ["angular-loading-bar", "ui.router", "ngAnimate", "ngStorage", "ngGrid", "ui.bootstrap", "ngFileUpload"]);
mainApp.config(function ($stateProvider, $urlRouterProvider) {
    routeConfig($stateProvider, $urlRouterProvider);
});

function routeConfig($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise("/");
    $stateProvider.state("home", {url: "/", templateUrl: "./home.html"})
        .state("menu.list", {url: "/list",templateUrl: "./module/system/menu/menuList.html",controller: "systemMenuListCtl"})
        .state("menu", {url: "/menu", templateUrl: "./module/system/menu/menu.html"})

        .state("user", {url: "/user", templateUrl: './module/system/user/user.html'})
        .state("user.list", {url: "/list", templateUrl: './module/system/user/userList.html', controller: "systemUserListCtl"})

        .state("user.profile", {url: "/profile", templateUrl: './module/system/user/userProfile.html', controller: "systemUserSettingProfileCtl"})
        .state("user.modify", {url: "/update", templateUrl: './module/system/user/userModifyPassword.html', controller: "systemUserSettingUpdateCtl"})


        .state("role", {url: "/role", templateUrl: './module/system/role/role.html'})
        .state("role.list", {url: "/list", templateUrl: './module/system/role/roleList.html', controller: "systemRoleListCtl"})

        .state("privilege", {url: "/privilege", templateUrl: './module/system/privilege/privilege.html'})
        .state("privilege.list", {url: "/list", templateUrl: './module/system/privilege/privilegeList.html', controller: "systemPrivilegeListCtl"})
    ;
}


