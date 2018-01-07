<?php
include('./helperfuncs.php');

if(isset($_SERVER['HTTP_USER_AGENT']) && !empty($_SERVER['HTTP_USER_AGENT']) && $_SERVER['HTTP_USER_AGENT']=='PhoneMonitor')
{
    if(isset($_POST['uniqueid']) && !empty($_POST['uniqueid']))
    {
        $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
        if($conn->connect_errno==0){
            $uniqueid=$conn->escape_string($_POST['uniqueid']);
            $commandid=(int) $conn->escape_string($_POST['commandid']);
            $param1=$conn->escape_string($_POST['param1']);
            $param2=$conn->escape_string($_POST['param2']);
            $param3=$conn->escape_string($_POST['param3']);
            $param4=$conn->escape_string($_POST['param4']);
            $output=$conn->escape_string(getResultString($_POST['output'],$commandid,$uniqueid));
            date_default_timezone_set('Asia/Kathmandu');
            $datetimenow=$conn->escape_string(date("Y-m-d H:i:s"));

            $udpatependingstatusquery="UPDATE commandlist SET Pending=0, ExecutedDateTime='$datetimenow', Result='$output'
            WHERE DeviceUniqueId='$uniqueid' AND Pending=1 AND CommandId=$commandid 
            AND Param1='$param1' AND Param2='$param2' AND Param3='$param3' AND Param4='$param4'";
            $response=$conn->query($udpatependingstatusquery);

            if($response)
            {
                echo("Table updated");
            }
        }
    }
}

?>