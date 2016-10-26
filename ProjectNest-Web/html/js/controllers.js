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
  		aboutdetails:  $sce.trustAsHtml("Contribute to New Zealand's goal to become <a href='http://www.doc.govt.nz/our-work/predator-free-new-zealand-2050/' class='linkColor'>predator free by 2050</a> by <a href='#/volunteer' class='linkColor'>volunteering</a> with your local branch of the Department of Conservation. You could help to reset predator traplines in the New Zealand bush and use the Nest NZ mobile application to record the predators caught.")
  	}, 
  	{  
  		header:'Data',
  		name:'TuiTwo',
  		photo: 'img/TuiTwo.jpg',
  		description:'View the data from the traplines where DOC volunteers use our services',
  		link:'/#/statistics',
  		aboutdetails: $sce.trustAsHtml('Connect with Project Nest NZ by <a href="#/contact" class="linkColor">contacting the core development team</a> or following us on <a href="https://twitter.com/NestNZ" class="linkColor">Twitter (@NestNZ)</a> or <a href="https://facebook.com/projectnestnz" class="linkColor">Facebook</a>.')
  	}, 
  	{
  		header:'Volunteers',
  		name: 'Kea',
  		photo: 'img/Kea.jpg',
  		description:'View information on how to sign up as a DOC volunteer.',
  		link:'/#/volunteer',
  		aboutdetails: $sce.trustAsHtml('Project Nest NZ is an open source project! Collaboration in any area - UX design, web design, mobile development etc. - is welcome. Please read through the project wiki on the public <a href="https://github.com/FrancisG-Massey/Capstone2016" class="linkColor">Github repository</a>, or <a href="#/contact" class="linkColor">contact the core development team, to get started!</a>')
  	}
  ];
  /*$scope.plusOne =function(index){
    $scope.products[index].likes+=1;
  };
  $scope.minusOne = function(index){
    $scope.products[index].dislikes+=1;
  };*/

}]);