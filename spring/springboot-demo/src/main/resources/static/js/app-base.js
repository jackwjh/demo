var _appContextPath = null;

// 拼接项目路径
function fullPath(path) {
    if (_appContextPath == null) {
        _appContextPath = $("#appContextPath").val();
    }
    return _appContextPath + path;
}

mainApp.controller("headerController", function($http, $scope){
    $scope.logout = function(){
        alert("logout");
    }
});