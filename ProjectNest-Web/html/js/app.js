'use strict';

// declare modules
angular.module('Authentication', []);
angular.module('Admin', []);


var myApp = angular
        .module('myApp', [ 'Authentication', 'Admin', 'ngRoute','ngCookies' ])

        .config([ '$httpProvider', function($httpProvider) {
            $httpProvider.defaults.useXDomain = true;
            delete $httpProvider.defaults.headers.common['X-Requested-With'];
        } ])
        .config([ '$routeProvider', function($routeProvider) {

        $routeProvider
        .when('/login', {
                controller : 'LoginController',
                templateUrl : 'modules/authentication/views/login.html?'+new Date().getTime()
            })
        .when('/', {
            controller : 'HomeController',
            templateUrl : 'templates/home.html?'+new Date().getTime()
        })
        .when('/about', {
            controller: 'HomeController',
            templateUrl: 'templates/about.html?'+new Date().getTime()
        })
        .when('/mobile', {
            controller: 'HomeController',
            templateUrl: 'templates/mobile.html?'+new Date().getTime()
        })
        .when('/contact', {
            controller: 'HomeController',
            templateUrl: 'templates/contact.html?'+new Date().getTime()
        })
        .when('/statistics', {
            controller: 'HomeController',
            templateUrl: 'templates/statistics.html?'+new Date().getTime()
        })
        .when('/volunteer', {
            controller: 'HomeController',
            templateUrl: 'templates/volunteer.html?'+new Date().getTime()
        })
        .when('/trapline-admin',{
            controller: 'AdminTraplineController',
            templateUrl: 'modules/admin/views/trapline-admin.html?'+new Date().getTime(),
            resolve: {
            	trapline: function($http, $route,$cookieStore,$rootScope,$location){
                    return $http({
                        method: "GET",
                        url: 'https://www.nestnz.org/api/trapline',
                        params: {
                            '_': new Date().getTime()
                        }
                    })
                    .then(function(response){
                        return response.data;
                    },function(errorResponse){
                    	if(errorResponse.status == 403){
                            $rootScope.globals = {};
                            $cookieStore.remove('globals');
                            $location.path('/login');
                    	} 	
                    }
                    );
                },
                region: function($http, $route,$cookieStore,$rootScope,$location){
                    return $http({
                        method: "GET",
                        url: 'https://www.nestnz.org/api/region',
                        params: {
                            '_': new Date().getTime()
                        }
                    })
                    .then(function(response){
                        return response;
                    },function(errorResponse){
                    	if(errorResponse.status == 403){
                            $rootScope.globals = {};
                            $cookieStore.remove('globals');
                            $location.path('/login');
                    	} 	
                    }
                    );
                },
                baits:function($http, $route,$cookieStore,$rootScope){
                        return $http
                        .get('https://www.nestnz.org/api/bait')
                        .then(function(response){
                            return response.data;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    })
                    },
                trap_type:function($http, $route,$cookieStore,$rootScope){
                        return $http
                        .get('https://www.nestnz.org/api/trap-type')
                        .then(function(response){
                            return response.data;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    })
                    }
        }
        })
        .when('/trapline-admin/add-newtrapline',{
            controller: 'AdminNewTraplineController',
            templateUrl: 'modules/admin/views/new_trapline.html?'+new Date().getTime(),
            resolve: {
            	 region:function($http, $route,$cookieStore,$rootScope){
                     return $http
                     .get('https://www.nestnz.org/api/region')
                     .then(function(response){
                         return response;
                 }, function(errorResponse){
                 	if(errorResponse.status==403){
                 		$rootScope.globals = {};
                 		$cookieStore.remove('globals');
                 	}
                 })
                 },
                baits:function($http, $route,$cookieStore,$rootScope){
                        return $http
                        .get('https://www.nestnz.org/api/bait')
                        .then(function(response){
                            return response.data;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    })
                    },
                trap_type:function($http, $route,$cookieStore,$rootScope){
                        return $http
                        .get('https://www.nestnz.org/api/trap-type')
                        .then(function(response){
                            return response.data;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    })
                    }
        }
        })
         .when('/trapline-admin/:traplineId/:traplineName/edit-trapline',{
            controller: 'AdminEditTraplineController',
            templateUrl: 'modules/admin/views/edit_trapline.html?'+new Date().getTime(),
            resolve: {
            	 region:function($http, $route,$cookieStore,$rootScope){
                     return $http
                     .get('https://www.nestnz.org/api/region')
                     .then(function(response){
                         return response;
                 }, function(errorResponse){
                 	if(errorResponse.status==403){
                 		$rootScope.globals = {};
                 		$cookieStore.remove('globals');
                 	}
                 })
                 },
            	baits:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/bait')
                    .then(function(response){
                        return response.data;
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                },
            trap_type:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/trap-type')
                    .then(function(response){
                        return response.data;
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                },
                trapline:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get("https://www.nestnz.org/api/trapline/"+$route.current.params.traplineId+"/_="+new Date().getTime())
                    .then(function(response){
                        return response.data;
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                }
        }
        })
        .when('/trap-admin/:traplineId/:traplineName', {
            controller: 'AdminTrapController',
            templateUrl: 'modules/admin/views/trap-admin.html?'+new Date().getTime(),
            resolve: {
            	traps: function($http, $route,$cookieStore,$rootScope){
                    return $http({
                        method: "GET",
                        url: 'https://www.nestnz.org/api/trap',
                        params: {
                            '_': new Date().getTime(),
                            'trapline-id': $route.current.params.traplineId
                        }
                    })
                    .then(function(response){
                        return response.data;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    });
                },
                baits:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/bait')
                    .then(function(response){
                        return response.data;
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                },
                trapline:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/trapline/'+$route.current.params.traplineId)
                    .then(function(response){
                        return response.data[0];
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                },
                trap_type:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/trap-type')
                    .then(function(response){
                        return response.data;
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                },
                catch_types:function($http,$route,$cookieStore,$rootScope){
                	return $http
                	.get('https://www.nestnz.org/api/catch-type')
                	.then(function(response){
                		return response.data;
                	}, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    })
                }
            }
        })
        .when('/trap-admin/:traplineId/:traplineName/add-new', {
            controller: 'AdminNewTrapController',
            templateUrl: 'modules/admin/views/new_trap.html?'+new Date().getTime(),
            resolve: {
                baits:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/bait')
                    .then(function(response){
                        return response.data;
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                },
                trap_type:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/trap-type')
                    .then(function(response){
                        return response.data;
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                },
                catch_types:function($http,$route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/catch-type')
                    .then(function(response){
                        return response.data;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    })
                }
            }
        })
         .when('/trap-admin/:trap_id/:traplineName/edit-trap', {
            controller: 'AdminEditTrapController',
            templateUrl: 'modules/admin/views/edit_trap.html',
            resolve: {
            	traps: function($http, $route,$cookieStore,$rootScope){
                    return $http({
                        method: "GET",
                        url: 'https://www.nestnz.org/api/trap',
                        params: {
                            '_': new Date().getTime(),
                            'trapline-id': $route.current.params.traplineId
                        }
                    })
                    .then(function(response){
                        return response.data;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    });
                },
            	baits:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/bait')
                    .then(function(response){
                        return response.data;
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                },
                trap_type:function($http, $route,$cookieStore,$rootScope){
                    return $http
                    .get('https://www.nestnz.org/api/trap-type')
                    .then(function(response){
                        return response.data;
                }, function(errorResponse){
                	if(errorResponse.status==403){
                		$rootScope.globals = {};
                		$cookieStore.remove('globals');
                	}
                })
                },                
            	trap: function($http, $route,$cookieStore,$rootScope){
                    return $http
                        .get('https://www.nestnz.org/api/trap/'+$route.current.params.trap_id)
                        .then(function(response){
                            return response.data;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    })
                }
            }
        })
        .when('/volunteer-admin/:traplineId/:traplineName', {
            controller: 'AdminVolunteerController',
            templateUrl: 'modules/admin/views/volunteer-admin.html?'+new Date().getTime(),
            resolve: {
            	trapline_users: function($http, $route,$cookieStore,$rootScope){
            		 return $http({
                         method: "GET",
                         url: 'https://www.nestnz.org/api/trapline-user',
                         params: {
                             '_': new Date().getTime(),
                             'trapline-id': $route.current.params.traplineId
                         }
                     })
                     .then(function(response){
                         return response.data;
                     }, function(errorResponse){
                     	if(errorResponse.status==403){
                     		$rootScope.globals = {};
                     		$cookieStore.remove('globals');
                     	}
                     });                    
                },
                users: function($http, $route,$cookieStore,$rootScope){
           		 return $http({
                        method: "GET",
                        url: 'https://www.nestnz.org/api/user',
                        params: {
                            '_': new Date().getTime()
                        }
                    })
                    .then(function(response){
                        return response;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    });                    
               }
            }
        })
        .when('/user-admin/add-user', {
            controller: 'AdminNewUserController',
            templateUrl: 'modules/admin/views/new_user.html?'+new Date().getTime()
        }) 
        
        .when('/user-admin/:userId/edit-user', {
            controller: 'AdminEditUserController',
            templateUrl: 'modules/admin/views/edit_user.html?'+new Date().getTime(),
            resolve: {
            user:function($http, $route,$cookieStore,$rootScope){
                return $http
                .get("https://www.nestnz.org/api/user/"+$route.current.params.userId+"/_="+new Date().getTime())
                .then(function(response){
                    return response.data;
            }, function(errorResponse){
            	if(errorResponse.status==403){
            		$rootScope.globals = {};
            		$cookieStore.remove('globals');
            	}
            });
            }
            }
            })
        
        .when("/user-admin",{
            controller: 'AdminUserController',
            templateUrl: 'modules/admin/views/user-admin.html?'+new Date().getTime(),
            resolve: {
            	traplines: function($http, $route,$cookieStore,$rootScope,$location){
                    return $http({
                        method: "GET",
                        url: 'https://www.nestnz.org/api/trapline',
                        params: {
                            '_': new Date().getTime()
                        }
                    })
                    .then(function(response){
                        return response.data;
                    },function(errorResponse){
                    	if(errorResponse.status == 403){
                            $rootScope.globals = {};
                            $cookieStore.remove('globals');
                            $location.path('/login');
                    	} 	
                    }
                    );
                },            	
            	trapline_users: function($http, $route,$cookieStore,$rootScope){
            		 return $http({
                         method: "GET",
                         url: 'https://www.nestnz.org/api/trapline-user',
                         params: {
                             '_': new Date().getTime()
                         }
                     })
                     .then(function(response){
                         return response.data;
                     }, function(errorResponse){
                     	if(errorResponse.status==403){
                     		$rootScope.globals = {};
                     		$cookieStore.remove('globals');
                     	}
                     });                    
                },
                users: function($http, $route,$cookieStore,$rootScope){
           		 return $http({
                        method: "GET",
                        url: 'https://www.nestnz.org/api/user',
                        params: {
                            '_': new Date().getTime()
                        }
                    })
                    .then(function(response){
                        return response.data;
                    }, function(errorResponse){
                    	if(errorResponse.status==403){
                    		$rootScope.globals = {};
                    		$cookieStore.remove('globals');
                    	}
                    });                    
               }
            }
        	
        })
        .otherwise({
            templateUrl : 'templates/404.html'
        });
    } ])
        .run(
                [
                        '$rootScope',
                        '$location',
                        '$cookieStore',
                        '$http',
                        function($rootScope, $location, $cookieStore, $http) {
                            // keep user logged in after page refresh
                            $('#navbar').collapse('hide');

                            $rootScope.globals = $cookieStore.get('globals')
                                    || {};
                            console.log($rootScope.globals.currentUser);
                            if ($rootScope.globals.currentUser) {
                                $http.defaults.headers.common['Session-Token'] = $rootScope.globals.currentUser.sessionToken; // jshint
                                                                                                                                // ignore:line

                            }

                            $rootScope.$on('$locationChangeStart', function(
                                    event, next, current) {
                                // redirect to login page if not logged in
                                if ($location.path() !== '/login'
                                        && !$rootScope.globals.currentUser) {
                                    // $location.path('/login');
                                }
                            });
                        } ])

        /*
         * .controller('BackButton',['$scope',function ($scope) { $scope.goBack =
         * function() { window.history.back(); }; }])
         */

        .directive(
                'bsActiveLink',
                [
                        '$location',
                        function($location) {
                            return {
                                restrict : 'A', // use as attribute
                                replace : false,
                                link : function(scope, elem) {
                                    // after the route has changed
                                    scope
                                            .$on(
                                                    "$routeChangeSuccess",
                                                    function() {
                                                        var hrefs = [
                                                                '/#'
                                                                        + $location
                                                                                .path(),
                                                                '#'
                                                                        + $location
                                                                                .path(), // html5:
                                                                                            // false
                                                                $location
                                                                        .path() ]; // html5:
                                                                                    // true
                                                        angular
                                                                .forEach(
                                                                        elem
                                                                                .find('a'),
                                                                        function(
                                                                                a) {
                                                                            a = angular
                                                                                    .element(a);
                                                                            if (-1 !== hrefs
                                                                                    .indexOf(a
                                                                                            .attr('href'))) {
                                                                                a
                                                                                        .parent()
                                                                                        .addClass(
                                                                                                'active');
                                                                            } else {
                                                                                a
                                                                                        .parent()
                                                                                        .removeClass(
                                                                                                'active');
                                                                            }
                                                                            ;
                                                                        });
                                                    });
                                }
                            }
                        } ]);