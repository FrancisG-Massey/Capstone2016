/**
 * 
 */
myApp.controller('HomeController', ['$scope', '$rootScope','$sce', function($scope, $rootScope,$sce) { 
  $rootScope.wrapClass = undefined;
  $scope.images = [
  	{  
    	header: 'Mobile Application',
  		name: 'WoodPigeon',
  		photo: 'img/WoodPigeonOriginal.jpg',
  		description:'We will describe our mobile app here and give information on how to download it.',
  		link:'/#/mobile',
  		aboutTitle:"Contribute",
  		aboutdetails:  $sce.trustAsHtml("Contribute to New Zealand's goal to become <a href='http://www.doc.govt.nz/our-work/predator-free-new-zealand-2050/' class='linkColor'>predator free by 2050</a> by <a href='#/volunteer' class='linkColor'>volunteering</a> with your local branch of the Department of Conservation. You could help to reset predator traplines in the New Zealand bush and use the Nest NZ mobile application to record the predators caught.")
  	}, 
  	{  
  		header:'Data',
  		name:'TuiTwo',
  		photo: 'img/TuiTwo.jpg',
  		description:'View the data from the traplines where DOC volunteers use our services',
  		link:'/#/statistics',
  		aboutTitle:"Connect",
  		aboutdetails: $sce.trustAsHtml('Connect with Project Nest NZ by <a href="#/contact" class="linkColor">contacting the core development team</a> or following us on <a href="https://twitter.com/NestNZ" class="linkColor">Twitter (@NestNZ)</a> or <a href="https://facebook.com/projectnestnz" class="linkColor">Facebook</a>.')
  	}, 
  	{
  		header:'Volunteers',
  		name: 'Kea',
  		photo: 'img/Kea.jpg',
  		description:'View information on how to sign up as a DOC volunteer.',
  		link:'/#/volunteer',
  		aboutTitle:"Collaborate",
  		aboutdetails: $sce.trustAsHtml('Project Nest NZ is an open source project! Collaboration in any area - UX design, web design, mobile development etc. - is welcome. Please read through the project wiki on the public <a href="https://github.com/FrancisG-Massey/Capstone2016" class="linkColor">Github repository</a>, or <a href="#/contact" class="linkColor">contact the core development team, to get started!</a>')
  	}
  ];
  $scope.mobileImages = [
	{
		title:"Trapline Selection",
		photo:"img/trapline_selection.png",
		description:"Users will be registered to traplines for which they volunteer, these traplines will be displayed on a selection screen according to their region. The user can select the trapline they are resetting and they will be taken to a screen displaying information on the trapline."
			
	},
	{
		title:"Map",
		photo:"img/map.png",
		description:"Use the inbuilt map system to locate traps. Maps can be pre-loaded so that all the features are able to be used when in the bush and without internet access. Vibration and/or sound will alert the user to their proximity to the next trap facilitating limited interaction with the mobile device while in the bush."
	},
	{
		title:"Select Navigation",
		photo:"img/select_navigation.png",
		description:"The user can select a certain range of traps to reset on a particular trapline. This feature also facilitates visiting traps in reverse order."
	},
	{
		title:"Log Catch",
		photo:"img/log_catch.png",
		description:"When the user resets a trap, log the animal that has been caught through the 'trap catch' screen. This screen features large buttons which can be easily pressed while wearing gloves."
	}
                    ];
  /*$scope.plusOne =function(index){
    $scope.products[index].likes+=1;
  };
  $scope.minusOne = function(index){
    $scope.products[index].dislikes+=1;
  };*/

}]);