'use strict';

angular
		.module('Admin')
		.controller(
				'AdminTraplineController',
				[
						'$scope',
						'$rootScope',
						'$http',
						'region',
						'trapline',
						'baits',
						'trap_type',
						'$route',
						'$location',
						function($scope, $rootScope, $http, region, trapline,
								baits, trap_type, $route, $location) {
							// console.log($scope.currentPath);
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.regions = region.data;
							$scope.traplines = trapline;
							// $scope.baits = baits;
							// $scope.trap_types = trap_type;
							console.log($scope.traplines);
							// Valid Trapline objects are nested in each region
							// object.
							var region, trapline;
							for (var i = 0; i < $scope.regions.length; i++) {
								region = $scope.regions[i];
								region.traplines = [];
								for (var x = 0; x < $scope.traplines.length; x++) {
									trapline = $scope.traplines[x];
									if (trapline.region_id == region.id) {
										region.traplines.push(trapline);
									}
								}
							};
							// loading / download csv data and save it to scope variable.
							$scope.loadcsv = function(trapline){
								console.log(trapline.id);
								var id = trapline.id;
							    var config = {
										method: 'GET',
						       			 url: 'https://www.nestnz.org/api/catch-report-simple?trapline-id='+id+'&_='+new Date().getTime(),
						       			 headers: {
						       			   'Accept': 'text/csv'
						       			 },
							            }
							   $http(config)
							            .success(function(res){
							                console.log(res);
							                $scope.csv_data = res;
							            })
							            .error(function(res){});
							  };
							  // load catch history in json format.
							  $scope.loadjson = function(trapline){
									console.log(trapline.id);
									var id = trapline.id;
								    var config = {
											method: 'GET',
							       			 url: 'https://www.nestnz.org/api/catch-report-simple?trapline-id='+id+'&_='+new Date().getTime(),
								            }
								     $http(config)
								            .success(function(res){
								                console.log(res);
								                $scope.json_data=res;
								            })
								            .error(function(res){});
								  };
									$scope.formatDate = function(date) {
										var dateOut = new Date(date);
										return dateOut;
									};
							} 	
					
						])
		.controller(
				'AdminTrapController',
				[
						'$scope',
						'$rootScope',
						'traps',
						'baits',
						'trap_type',
						'$route',
						'$http',
						'catch_types',
						function($scope, $rootScope, traps, baits, trap_type,
								$route, $http, catch_types) {
							// var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_id = $route.current.params.traplineId;
							$scope.trapline_name = $route.current.params.traplineName;
							$scope.traps = traps;
							$scope.baits = baits;
							$scope.trap_type = trap_type;
							// get all catch types
							$scope.catch_types = catch_types;

							// formatting Date
							$scope.formatDate = function(date) {
								var dateOut = new Date(date);
								return dateOut;
							};

							// Define length of traps per each page.
							// pagination
							var pageLength = 10, numTraps = traps.length, trapPages = [], pages = Math
									.ceil(numTraps / pageLength);

							for (var x = 0; x < pages; x++) {
								trapPages[x] = [];
							}

							var page, trap;
							for (var i = 0; i < traps.length; i++) {
								trap = traps[i];
								page = Math.floor(i / pageLength);
								trapPages[page].push(trap);
							}
							$scope.traps = trapPages;
							$scope.currentPage = 0;
							var mymap;

							$scope.gap = 5;

							$scope.range = function(size, start, end) {
								var ret = [];

								if (size < end) {
									end = size;
									start = size - $scope.gap;
									start = start < 0 ? 0 : start;
									// console.log(start);
								}
								for (var i = start; i < end; i++) {
									ret.push(i);
								}
								// console.log(ret);
								return ret;
							};

							$scope.prevPage = function() {
								if ($scope.currentPage > 0) {
									$scope.currentPage--;
								}
							};

							$scope.nextPage = function() {
								if ($scope.currentPage < $scope.traps.length - 1) {
									$scope.currentPage++;
								}
							};

							$scope.setPage = function() {
								// console.log(this.n);
								$scope.currentPage = this.n;
							};

							$('#trapline-map').on('shown.bs.modal', function() {
								setTimeout(function() {
									mymap.invalidateSize();
								}, 1);
							});

							// for view traps on a map
							$scope.showMap = function() {
								console.log($scope.traps[0]);
								if (!mymap) {
									mymap = L.map('mapid');
									var trap;

									mymap.setView([ traps[0].coord_lat,
											traps[0].coord_long ], 13);

									L
											.tileLayer(
													'https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}',
													{
														attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery <a href="http://mapbox.com">Mapbox</a>',
														maxZoom : 18,
														id : 'mrmjlee.182flnof',
														accessToken : 'pk.eyJ1IjoibXJtamxlZSIsImEiOiJjaXNlOTNwNDYwMDlnMnlydHViZ3dpMmt6In0.miWLZ3CSlid3NaTw1KtRDg'
													}).addTo(mymap);

									var marker, popupText;
									for (var i = 0; i < traps.length; i++) {
										trap = traps[i];
										popupText = "<strong>Trap: " + trap.id
												+ '</strong><br>'
												+ trap.coord_lat + ' N<br>'
												+ trap.coord_long + ' E';
										trap.popup = L.marker(
												[ trap.coord_lat,
														trap.coord_long ])
												.addTo(mymap).bindPopup(
														popupText);
									}

								}

								// mapDefault();
								return true;
							};
						} ])

		.controller(
				'AdminVolunteerController',
				[
						'$scope',
						'$rootScope',
						'trapline_users',
						'users',
						'$route',
						function($scope, $rootScope, trapline_users, users,
								$route) {
							// var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_id = $route.current.params.traplineId;
							$scope.trapline_name = $route.current.params.traplineName;
							$scope.trapline_users = trapline_users;
							console.log($scope.trapline_users);
							$scope.users = users.data;
							console.log($scope.users);

							var usersForTrapLine = [], trapLineUser, user;
							for (var i = 0; i < $scope.trapline_users.length; i++) {
								trapLineUser = $scope.trapline_users[i];
								for (var x = 0; x < $scope.users.length; x++) {
									user = $scope.users[x];
									if (user.id == trapLineUser.user_id) {
										usersForTrapLine.push(user);
									}
								}
							}
							;

							$scope.formatDate = function(date) {
								var dateOut = new Date(date);
								return dateOut;
							};

							$scope.addNew = function() {
								$scope.selected = false;
							};

							var pageLength = 10, numVolunteers = usersForTrapLine.length, newArray = [], pages = Math
									.ceil(numVolunteers / pageLength);

							for (var x = 0; x < pages; x++) {
								newArray[x] = [];
							}

							var page, volunteer;
							for (var i = 0; i < usersForTrapLine.length; i++) {
								volunteer = usersForTrapLine[i];
								page = Math.floor(i / pageLength);
								newArray[page].push(volunteer);
							}
							$scope.currentPage = 0;
							$scope.volunteers = newArray;

							$scope.setSelected = function(item) {
								$scope.selected = this.volunteer;
							};

							$scope.gap = 5;

							$scope.range = function(size, start, end) {
								var ret = [];

								if (size < end) {
									end = size;
									start = size - $scope.gap;
									start = start < 0 ? 0 : start;
									// console.log(start);
								}
								for (var i = start; i < end; i++) {
									ret.push(i);
								}
								// console.log(ret);
								return ret;
							};

							$scope.prevPage = function() {
								if ($scope.currentPage > 0) {
									$scope.currentPage--;
								}
							};

							$scope.nextPage = function() {
								if ($scope.currentPage < $scope.volunteers.length - 1) {
									$scope.currentPage++;
								}
							};

							$scope.setPage = function() {
								// console.log(this.n);
								$scope.currentPage = this.n;
							};

						} ])
		.controller(
				'AdminNewTrapController',
				[
						'$scope',
						'$rootScope',
						'baits',
						'trap_type',
						'$route',
						'$http',
						'catch_types',
						function($scope, $rootScope, baits, trap_type, $route,
								$http, catch_types) {
							// var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_id = $route.current.params.traplineId;
							$scope.baits = baits;
							$scope.trap_type = trap_type;
							$scope.catch_types = catch_types;
							$scope.trapline_name = $route.current.params.traplineName;
							;

							$scope.Save = function() {
								// as json object
								var data = {
									"trapline_id" : parseInt($scope.trapline_id),
									"number" : $scope.trapNumber,
									"coord_long" : $scope.longtitude,
									"coord_lat" : $scope.latitude,
									"traptype_id" : $scope.typeId,
									"status" : $scope.status,
									"bait_id" : $scope.baitId
								};

								$http.post('https://www.nestnz.org/api/trap',
										data).then(
										function(data, status, header, config) {
											$route.reload();
										});
							};
						} ])
		.controller(
				'AdminEditTrapController',
				[
						'$scope',
						'$rootScope',
						'baits',
						'trap_type',
						'$route',
						'$http',
						'trap',
						'$location',
						'traps',
						function($scope, $rootScope, baits, trap_type, $route,
								$http, trap, $location, traps) {
							// var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_name = $route.current.params.traplineName;
							;
							$scope.trap = trap[0];
							// $scope.trap.id = $route.current.params.trap_id;
							$scope.trapline_id = $scope.trap.trapline_id;
							$scope.baits = baits;
							$scope.trap_type = trap_type;
							console.log(trap);
							$scope.Edit = function() {
								console.log($scope.trap.id + " id");
								console.log($scope.trap.coord_lat + " lati");
								console.log($scope.trap.coord_long + " long");
								console.log($scope.trap.status + " status");
								console.log($scope.trap.bait_id + " bait id");
								console.log($scope.trap.traptype_id
										+ " traptype id");
								console.log($scope.trap);

								$http
										.put(
												'https://www.nestnz.org/api/trap/'
														+ $scope.trap.id,
												$scope.trap)
										.then(
												function(data, status, header,
														config) {
													$location
															.path("/trap-admin/"
																	+ $scope.trapline_id
																	+ "/"
																	+ $scope.trapline_name);
												});

								/*
								 * $http.post('https://www.nestnz.org/api/trap',data)
								 * .then(function(data,status,header,config) {
								 * $route.reload(); });
								 */
							};
							var traps = traps;
							var focus_trap;
							var mymap;
							$scope.showMap = function() {
								if (!mymap) {
									mymap = L.map('mapid'), trap;
									// $scope.trap.popup.openPopup();
									// mymap.setView([traps[0].coord_lat,
									// traps[0].coord_long], 13);

									L
											.tileLayer(
													'https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}',
													{
														attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery <a href="http://mapbox.com">Mapbox</a>',
														maxZoom : 18,
														id : 'mrmjlee.182flnof',
														accessToken : 'pk.eyJ1IjoibXJtamxlZSIsImEiOiJjaXNlOTNwNDYwMDlnMnlydHViZ3dpMmt6In0.miWLZ3CSlid3NaTw1KtRDg'
													}).addTo(mymap);

									var marker, popupText;
									for (var i = 0; i < traps.length; i++) {
										trap = traps[i];
										if (trap.id == $scope.trap.id) {
											focus_trap = trap;
										}
										popupText = "<strong>Trap: "
												+ trap.number + '</strong><br>'
												+ trap.coord_lat + ' N<br>'
												+ trap.coord_long + ' E';
										trap.popup = L.marker(
												[ trap.coord_lat,
														trap.coord_long ])
												.addTo(mymap).bindPopup(
														popupText);
									}
								}
								// var popupText = "<strong>Trap: " +
								// focus_trap.number + '</strong><br>' +
								// focus_trap.coord_lat + ' N<br>' +
								// focus_trap.coord_long + ' E';

								// focus_trap.popup =
								// L.marker([focus_trap.coord_lat,
								// focus_trap.coord_long]).addTo(mymap).bindPopup(popupText);
								console.log(focus_trap);
								focus_trap.popup.openPopup();
								mymap.setView([ focus_trap.coord_lat,
										focus_trap.coord_long ], 13);
								return true;
							}

						} ])
		.controller(
				'AdminNewTraplineController',
				[
						'$scope',
						'$rootScope',
						'$http',
						'region',
						'baits',
						'trap_type',
						'$route',
						function($scope, $rootScope, $http, region, baits,
								trap_type, $route) {
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.regions = region.data;
							$scope.baits = baits;
							$scope.trap_types = trap_type;

							$scope.Save = function() {
								console.log($scope.region_id);
								console.log($scope.line_name);
								console.log($scope.startTag);
								console.log($scope.endTag);
								console.log($scope.baitId);
								console.log($scope.traptypeId);

								var data = {
									"name" : $scope.line_name,
									"region_id" : parseInt($scope.region_id),
									"start_tag" : $scope.startTag,
									"end_tag" : $scope.endTag,
									"img_filename" : null,
									"default_bait_id" : $scope.baitId,
									"default_traptype_id" : $scope.traptypeId

								};

								$http.post(
										'https://www.nestnz.org/api/trapline',
										data).then(
										function(data, status, header, config) {
											$route.reload();
										});
							};

						} ])
		.controller(
				'AdminNewVolunteerController',
				[
						'$scope',
						'$rootScope',
						'trapline_users',
						'users',
						'$route',
						function($scope, $rootScope, trapline_users, users,
								$route) {
							//var traplineId = $routeParams.traplineId;
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_id = $route.current.params.traplineId;
							$scope.trapline_users = trapline_users;
							console.log($scope.trapline_users);
							$scope.users = users.data;
							console.log($scope.users);

						} ])
		.controller(
				'AdminEditTraplineController',
				[
						'$scope',
						'$rootScope',
						'$http',
						'region',
						'baits',
						'trap_type',
						'$route',
						'trapline',
						'$location',
						function($scope, $rootScope, $http, region, baits,
								trap_type, $route, trapline, $location) {
							$rootScope.wrapClass = undefined;
							$rootScope.hideHeader = true;
							$scope.trapline_id = $route.current.params.traplineId;
							$scope.trapline_name = $route.current.params.traplineName;
							$scope.regions = region.data;
							$scope.baits = baits;
							$scope.trap_types = trap_type;
							$scope.trapline = trapline[0];
							console.log($scope.trapline);
							$scope.Edit = function() {
								$scope.trapline.default_bait_id=parseInt($scope.trapline.default_bait_id);
								$scope.trapline.traptype_id=parseInt($scope.trapline.traptype_id);
								console.log($scope.trapline);
								/*var data = {
										 "name": $scope.line_name,
								         "region_id": parseInt($scope.region_id),
								         "start_tag": $scope.startTag,
								         "end_tag": $scope.endTag,
								         "img_filename": null,
								         "default_bait_id": $scope.baitId,
								         "default_traptype_id": $scope.traptypeId

								};	*/

								$http.put(
										'https://www.nestnz.org/api/trapline/'
												+ $scope.trapline.id,
										$scope.trapline).then(
										function(data, status, header, config) {
											$location.path('/trapline-admin');
										});
							};

						} ]);
