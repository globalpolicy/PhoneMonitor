<?php
include('./helperfuncs.php');
//user-agent check is not implemented here bcoz this script is also called by web front-end besides the app
if(isset($_POST['uniqueid']) && !empty($_POST['uniqueid']))
{
    $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
    if($conn->connect_errno==0){
        CreateSettingsTableIfNotExists($conn);
        $uniqueid=$conn->escape_string($_POST['uniqueid']);
        $getsettingsquery="SELECT * FROM settings WHERE DeviceUniqueId='$uniqueid'";
        $response=$conn->query($getsettingsquery);
        if($response->num_rows>0)
        {
            if($row=$response->fetch_assoc()){
                $forceWifiOnForRecordUpload=$row['ForceWifiOnForRecordUpload'];
                $serverTalkInterval=$row['ServerTalkInterval'];
                
                $outputjsonobject=array('ForceWifiOnForRecordUpload'=>$forceWifiOnForRecordUpload, 
                'ServerTalkInterval'=>$serverTalkInterval);
                
                echo(json_encode($outputjsonobject));
            }
            else
            {
                echo('No row in settings table');
            }
            
        }
    }
    else
    {
        echo("Error connecting to database! $conn->connect_errno");
    }
}

exit();
?>