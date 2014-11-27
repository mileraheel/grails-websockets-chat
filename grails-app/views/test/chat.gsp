<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="chat-main">
		<g:set var="entityName" value="${message(code: 'admin.label', default: 'Admin')}" />
		<title><g:message code="default.create.label" args="[entityName,BAH,BAH]" /></title>
	</head>
	<body>
    <div class="container">
    <div class="row">
        <div class="col-md-12 top-bar">
            <div class="pull-left"><h1>Chat Application</h1></div>
            <div class="input-group input-group-sm col-md-3 pull-right">
                <input type="text" class="form-control" id="username">
                <span class="input-group-btn">
                    <button class="btn btn-default" type="button" onclick="selectUsername()">login</button>
                </span>
            </div><!-- /input-group -->
        </div>

    </div>

    <div class="row">
        <div class="col-md-3 left-sidebar">
            <ul class="nav nav-pills nav-stacked" id="userList">

            </ul>
        </div>
        <div class="cold-md-9">
           <div id="chatList">
               <div class="right-sidebar chat-area" id="room">
               </div>
           </div>


            <div class="cold-md-9 right-padding-botton">
                <div class="input-group">
                    <input type="text" class="form-control send-message" id="message" disabled="disabled">
                    <span class="input-group-btn">
                        <button class="btn btn-default send-message" type="button" onclick="sendMessage()" disabled="disabled">SEND</button>
                    </span>
                </div><!-- /input-group -->
            </div><!-- /.col-lg-6 -->

        </div>
    </div>

        </div>
	<br>


	
	<script type="text/javascript">

        var colorNewMessage = '#FA8072'
        var colorNormalText = "#FFFFFF"
        var colorSelected = "#C0C0C0"

        var webSocket=new WebSocket("ws://192.168.0.175:8080/grails-websocket-example/chatroomServerEndpoint");
		var messagesTextarea=document.getElementById("messagesTextarea");
        webSocket.onmessage=function(message) {processMessage(message);};
        webSocket.onopen=function(message) {processOpen(message);};
		webSocket.onclose=function(message) {processClose(message);};
		webSocket.onerror=function(message) {processError(message);};

		function processOpen(message) {
            //alert("process open is called...")
			//messagesTextarea.value +=" Server Connect.... "+"\n";
		}

		function sendMessage() {
          if (to != "room") { //locally add your sent message in the desired user window, because message will only go to that user, will not come back to you
              $( "#"+to).append("<p><b>me -> "+$("#message").val()+"</b></p>")
          }
		  webSocket.send(JSON.stringify({message:$("#message").val(), to:to, from:$("#username").val()}));
            $("#message").val('')
		}

        function processMessage(message) {

            var jsonData=JSON.parse(message.data)

            //if message is for the room
            if (jsonData.to == "room") {
                $("#room").append("<p><b>"+jsonData.message+"</b></p>")
                if ($("#room").hasClass("hide")){
                    $("li[name='room']").css("background-color",colorNewMessage)
                }


            //if private message
            } else if (jsonData.from!=null) {
                if ( !$("#"+jsonData.from).length ) {  //if chat area from user doesnt exist then create one and hide it
                    $("#chatList").append("<div class='right-sidebar chat-area hide' id="+jsonData.from+">")
                }
                if ($("#"+jsonData.from).hasClass("hide")){   //if the new chat area is hidden then highlight the user
                    $("li[name="+jsonData.from+"]").css("background-color",colorNewMessage)
                }
                $( "#"+jsonData.from).append("<p><b>"+jsonData.message+"</b></p>") // add message in the desired chat area
                }

            // if some user connected or disconnected
            if (jsonData.list!=null) {
                $("#userList").empty()
                $("#userList").append("<li name='room'><a href='#' onclick='chatUserClicked($(this).text())'>room</a></li>")
                $.each(jsonData.list, function(i, item) {
                    $("#userList").append("<li name='"+item+"'><a href='#' onclick='chatUserClicked($(this).text())'>"+item+"</a></li>")
                });
            }

        }
		function processClose(message) {
			webSocket.send(JSON.stringify({disconnect:$("#username").val()}));
		}
		function processError(message) {
			messagesTextarea.value +=" Error.... \n";
		}

        function selectUsername() {
            if ($("#username").val() != "") {
                webSocket.send(JSON.stringify({username:$("#username").val()}))
                $(".send-message").removeAttr('disabled')
            }
        }

        $("#username").keypress(function(e) {
            if(e.which == 13) {
                selectUsername()
            }
        });

        $("#message").keypress(function(e) {
            if(e.which == 13) {
                sendMessage()
            }
        });

        function chatUserClicked(name) {
            to = name
            $("li").css("background-color",colorNormalText)
            $("li[name="+name+"]").css("background-color",colorSelected)
            $(".chat-area").addClass("hide")
            if ( $( "#"+name).length ) {
                $( "#"+name).removeClass("hide")

            } else {
              $("#chatList").append("<div class='right-sidebar chat-area' id="+name+">")
            }
            //alert(name)
        }

        var to = "room"

    </script>
	</body>
	</html>