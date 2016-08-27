'use strict';
 
angular.module('Admin')
 
.controller('AdminController',['$scope', '$rootScope',function ($scope,$rootScope) {
		$rootScope.wrapClass = undefined;
    	$scope.traps = [
{
      "id" : 123,
      "line_id" : 2345678910,
      "coord_lat" : -40.314206,
      "coord_long" : 175.779946,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:26:07",
      "last_reset" : "2016-08-16T10:28:07",
      "bait_id" : 123456789101112,
      "number" : 1
},
{
      "id" : 254,
      "line_id" : 12345678910,
      "coord_lat" : -40.311086,
      "coord_long" : 175.775306,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:30:07",
      "last_reset" : "2016-08-16T10:30:07",
      "bait_id" : 123456789101112,
      "number" : 2
},
{
      "id" : 388,
      "line_id" : 12345678910,
      "coord_lat" : -40.311821,
      "coord_long" : 175.775993,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:35:07",
      "last_reset" : "2016-08-16T10:35:07",
      "bait_id" : 123456789101112,
      "number" : 3
},
{
      "id" : 367,
      "line_id" : 12345678910,
      "coord_lat" : -40.311821,
      "coord_long" : 175.775993,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:36:07",
      "last_reset" : "2016-08-16T10:36:07",
      "bait_id" : 123456789101112,
      "number" : 4
},
{
      "id" : 433,
      "line_id" : 12345678910,
      "coord_lat" : -40.312105,
      "coord_long" : 175.778328,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:37:07",
      "last_reset" : "2016-08-16T10:37:07",
      "bait_id" : 123456789101112,
      "number" : 5
},
{
      "id" : 587,
      "line_id" : 12345678910,
      "coord_lat" : -40.312119,
      "coord_long" : 175.777347,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:40:07",
      "last_reset" : "2016-08-16T10:40:07",
      "bait_id" : 123456789101112,
      "number" : 6
},
{
      "id" : 635,
      "line_id" : 12345678910,
      "coord_lat" : -40.309037,
      "coord_long" : 175.772448,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:44:07",
      "last_reset" : "2016-08-16T10:44:07",
      "bait_id" : 123456789101112,
      "number" : 7
},
{
      "id" : 754,
      "line_id" : 12345678910,
      "coord_lat" : -40.309705,
      "coord_long" : 175.772583,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:47:07",
      "last_reset" : "2016-08-16T10:47:07",
      "bait_id" : 123456789101112,
      "number" : 8
},
{
      "id" : 867,
      "line_id" : 12345678910,
      "coord_lat" : -40.310519,
      "coord_long" : 175.774827,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:55:07",
      "last_reset" : "2016-08-16T10:55:57",
      "bait_id" : 123456789101112,
      "number" : 9
},
{
      "id" : 967,
      "line_id" : 12345678910,
      "coord_lat" : -40.309849,
      "coord_long" : 175.773379,
      "type_id" : 1234567891011,
      "status" : "active",
      "created" : "2016-04-16T10:57:07",
      "last_reset" : "2016-08-16T10:57:07",
      "bait_id" : 123456789101112,
      "number" : 10
}];
    }]);