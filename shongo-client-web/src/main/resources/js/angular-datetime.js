/**
 * Date/Time module.
 */
var dateTimeModule = angular.module('ngDateTime', []);

/**
 * Initialize date/time picker.
 */
$(function () {
    if ( $.fn.datetimepicker != null) {
        $.fn.datetimepicker.dates['en'].today = 'Now';
    }
});

/**
 * Date/Time picker
 */
dateTimeModule.directive('dateTimePicker', function() {
    return {
        restrict: 'A',
        link: function postLink(scope, element, attrs, controller) {
            // Create date/time picker
            element.datetimepicker({
                pickerPosition: "bottom-left",
                weekStart: 1,
                minuteStep: 2,
                autoclose: true,
                todayBtn: true,
                todayHighlight: true
            });

            // Create method for initializing "datetime" or "date" format
            var dateTimePicker = element.data("datetimepicker");
            dateTimePicker.setFormatDate = function() {
                dateTimePicker.minView = $.fn.datetimepicker.DPGlobal.convertViewMode('month');
                dateTimePicker.viewSelect = element.data("datetimepicker").minView;
                dateTimePicker.setFormat("yyyy-mm-dd");
                if (element.val() != "") {
                    dateTimePicker.setValue();
                }
            };
            dateTimePicker.setFormatDateTime = function() {
                dateTimePicker.minView = $.fn.datetimepicker.DPGlobal.convertViewMode('hour');
                dateTimePicker.setFormat("yyyy-mm-dd hh:ii");
                if (element.val() != "") {
                    dateTimePicker.setValue();
                }
            };

            if ( attrs.format == "date") {
                dateTimePicker.setFormatDate();
            }
            else {
                dateTimePicker.setFormatDateTime();
            }
        }
    }
});

/**
 * Date picker
 */
dateTimeModule.directive('datePicker', function() {
    return {
        restrict: 'A',
        link: function postLink(scope, element, attrs, controller) {
            // Create date/time picker
            element.datetimepicker({
                pickerPosition: "bottom-left",
                weekStart: 1,
                minuteStep: 2,
                autoclose: true,
                todayBtn: true,
                todayHighlight: true
            });

            // Create method for initializing "date" format
            var dateTimePicker = element.data("datetimepicker");
            dateTimePicker.setFormatDate = function() {
                dateTimePicker.minView = $.fn.datetimepicker.DPGlobal.convertViewMode('month');
                dateTimePicker.viewSelect = element.data("datetimepicker").minView;
                dateTimePicker.setFormat("yyyy-mm-dd");
                if (element.val() != "") {
                    dateTimePicker.setValue();
                }
            };
            dateTimePicker.setFormatDate();
        }
    }
});

/**
 * Time picker
 */
dateTimeModule.directive('timePicker', function() {
    return {
        restrict: 'A',
        link: function postLink(scope, element, attrs, controller) {
            // Create date/time picker
            element.timepicker({
                //pickerPosition: "bottom-left",
                //format: "HH:ii p",
                //weekStart: 1,
                //minuteStep: 2,
                //autoclose: true,
                //todayBtn: true,
                ////todayHighlight: true
                //minuteStep: 5
                //showInputs: false,
                //disableFocus: true
                //minuteStep: 1,
                //template: 'modal',
                //appendWidgetTo: 'body',
                //showMeridian: false
                //defaultTime: false
                format: 'LT'
            });

            // Create method for initializing "date" format
            //var dateTimePicker = element.data("datetimepicker");
            //dateTimePicker.setFormatDate = function() {
            //    dateTimePicker.minView = $.fn.datetimepicker.DPGlobal.convertViewMode('hour');
            //    console.debug(dateTimePicker.minView);
            //    dateTimePicker.viewSelect = element.data("datetimepicker").minView;
            //    dateTimePicker.setFormat("hh:ii");
            //    if (element.val() != "") {
            //        dateTimePicker.setValue();
            //    }
            //};
            //dateTimePicker.setFormatDate();
        }
    }
});
