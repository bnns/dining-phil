function ChatCtrl($scope, socket)
{	     
    $scope.messages.get(function(){
        $.getJSON('/messages', function(items){
            var ul = $('<ul>');
            $.each(items, function(item){
                var li = $('<li>').text(item.title);
                ul.append(li);
            });

            $('body').append(ul);
        });
    });

}