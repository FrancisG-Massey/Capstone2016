/**
 * 
 */
app.controller('MainController', ['$scope', function($scope) { 
  $scope.images = [
  	{  
    	header: 'Mobile Application',
  		name: 'WoodPigeon',
  		photo: 'img/WoodPigeon.jpg',
  		description:'We will describe our mobile app here and give information on how to download it.'
  	}, 
  	{  
  		header:'Data',
  		name:'TuiTwo',
  		photo: 'img/TuiTwo.jpg',
  		description:'View the data from the traplines where DOC volunteers use our services'
  	}, 
  	{
  		header:'volunteers',
  		name: 'Kea',
  		photo: 'img/Kea.jpg',
  		description:'View profiles of hardworker DOC volunteers from around New Zealand.'
  	}
  ];
  /*$scope.plusOne =function(index){
    $scope.products[index].likes+=1;
  };
  $scope.minusOne = function(index){
    $scope.products[index].dislikes+=1;
  };*/

}]);