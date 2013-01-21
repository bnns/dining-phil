function ChatCtrl($scope, socket) {	
    $scope.messages = [
    {text:'hello world'},
    {text:'foo bar'}];

    socket.on('send:message', function (message) {
    	$scope.messages.push(message);
  	});

    $scope.startSim = function() {
        $scope.messages.push({text:'start'});
    };
    
    $scope.stopSim = function() {
        $scope.messages.push({text:'stop'});
    };

    $scope.getMessages = function() {
        $.getJSON('http://swift:9000/messages', function(items){
            $.each(items, function(item){
           		$scope.messages.push({text: items[item]});
            });
        });
    };

	$http.jsonp('http://swift:9000/messages').success(function(item));

}