/**
 * filterHead: table head @NotNull
 * filterList: table body @NotNull
 * filterName: search propertyName @NotNull
 * selectedList: select result list @return
 * isWatchList: is need watch filterList change(for ajax) @NotNeed can use angular.extend(scope.filterList, response.list) replace
 *
 * filterHead: [
 *  {
 *    displayName: '城市',
 *    propertyName: 'city',
 *    order: 1
 *  },
 *  {
 *    displayName: '人数',
 *    propertyName: 'personCount',
 *    order: 2
 *  }
 * ]
 *
 * filterList:[
 *  {
 *    city: '北京'
 *  },
 *  {
 *    personCount: '1000'
 *  }
 * ]
 */
app.directive('tableRowSelect', function ($filter,$timeout) {
  return{
    restrict: 'EA',
    scope:{
      filterHead: '=head',
      filterList: '=list',
      filterName: '=name',
      selectedList: '=selected',
      isWatchList: '='
    },
    templateUrl:'htmls/table.row.select.html',
    link: function (scope, elem) {
      scope.filterHead = $filter('orderBy')(scope.filterHead, "order");

      if(!scope.selectedList) scope.selectedList = [];
      scope.list = scope.filterList;

      if(scope.isWatchList) {
        scope.$watch('filterList', function () {
          scope.list = scope.filterList;
        });
      }

      scope.$watch('filterValue', function (newValue, oldValue) {
        if(!newValue) {
          scope.list = scope.filterList;
          return
        }
        if(newValue == oldValue) return;

        var tmp = [];
        scope.filterList.forEach(function (item) {
          if(item[scope.filterName].indexOf(newValue) >= 0)tmp.push(item);
        })
        scope.list = tmp;
      });

      scope.toggleItem = function (item) {
        if(item.checked){
          if(scope.selectedList.indexOf(item) < 0) scope.selectedList.push(item);
        }else{
          var index = scope.selectedList.indexOf(item);
          if(index >= 0) scope.selectedList.splice(index, 1);
        }
      };

      scope.toggleAll = function () {
        if(scope.checkedAll){
          scope.list.forEach(function (item) {
            item.checked = true;
            scope.toggleItem(item);
          })
        }else{
          scope.list.forEach(function (item) {
            item.checked = false;
            scope.toggleItem(item);
          })
        }
      };
    }
  }
});

//日期选择下拉框
//支持选择到月
app.directive('selectMonth', function ($modal, commonService, growl) {
  return {
    restrict: 'EA',
    replace: false,
    scope: {
      ngModel: "="
    },
    templateUrl: 'htmls/select-month.html',
    link: function (scope, element, attrs) {
      scope.years = [];
      scope.months = [];
      //当前时间
      var date = new Date();
      scope.year = date.getFullYear();
      scope.month = date.getMonth();

      //当前为1月份，显示去年的12月份
      if (scope.month <= 0) {
        scope.year = scope.year - 1;
        scope.month = 12;
      }

      scope.month = scope.month < 10 ? '0' + scope.month : scope.month;

      for (var i = 2013; i <= scope.year; i++) {
        scope.years.push(i);
      };

      for (var j = 1; j <= 12; j++) {
        var item = j < 10 ? "0" + j : j;
        scope.months.push(item);
      };

      scope.ngModel = scope.year + "-" + scope.month;
      scope.change = function () {
        scope.ngModel = scope.year + "-" + scope.month;
      };
    }
  }
});

/**
 * ng-bind-html中使用ng-model等angular指令不生效的解决办法
 */
app.directive('compile', function ($compile, $timeout) {
  return {
    restrict: 'A',
    link: function (scope, elem, attrs) {
      $timeout(function () {
        $compile(elem.contents())(scope);
      });
    }
  };
});
