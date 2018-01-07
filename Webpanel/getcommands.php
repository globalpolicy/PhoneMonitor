<?php
include('./helperfuncs.php');
if(isset($_SERVER['HTTP_USER_AGENT']) && !empty($_SERVER['HTTP_USER_AGENT']) && $_SERVER['HTTP_USER_AGENT']=='PhoneMonitor')
{
    if(isset($_POST['uniqueid']) && !empty($_POST['uniqueid']))
    {
        $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
        if($conn->connect_errno==0){
            CreateCommandlistTableIfNotExists($conn);
            $uniqueid=$conn->escape_string($_POST['uniqueid']);
            $getpendingcommandsquery="SELECT CommandId, Param1, Param2, Param3, Param4 FROM commandlist
            WHERE DeviceUniqueId='$uniqueid' AND Pending=1";
            $response=$conn->query($getpendingcommandsquery);
            if($response->num_rows>0)
            {
                $outputjsonarray=array();
                while($row=$response->fetch_assoc())
                {
                    $commandid=$row['CommandId'];
                    $param1=$row['Param1'];
                    $param2=$row['Param2'];
                    $param3=$row['Param3'];
                    $param4=$row['Param4'];
                    $outputjsonarray[]=array('commandid'=>$commandid, 'param1'=>$param1, 'param2'=>$param2,
                    'param3'=>$param3, 'param4'=>$param4);
                }
                echo(json_encode($outputjsonarray));
            }
        }
        else
        {
            echo("Error connecting to database! $conn->connect_errno");
        }
    }
}
exit();
?>