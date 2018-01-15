<?php
$DB_NAME='phonemonitor';
$DB_USERNAME='XXXX';
$DB_PASSWORD='XXXX';
$DB_HOST='localhost';

function checkUserPassword($username,$password)
{
    global $DB_NAME,$DB_USERNAME,$DB_PASSWORD,$DB_HOST;
    $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
    if($conn->connect_errno==0)
    {
        CreateUsersTableIfNotExists($conn);
        $username_escaped=$conn->escape_string($username);
        $getusersquery="SELECT * FROM users WHERE Username='$username_escaped'";
        $response=$conn->query($getusersquery);
        if($response)
        {
            while($row=$response->fetch_assoc())
            {
                if($row['Approved']==1)
                {
                    if(password_verify($password, $row['Passwordhash']))
                    {
                        return 'Ok';
                    }
                    else
                    {
                        return 'Wrong password';
                    }
                }
                else
                {
                    return 'User not approved yet';
                }
            }
        }
    }
    else
    {
        return 'Cannot connect to database';
    }
    return 'User does not exist!';
}

function CreateUsersTableIfNotExists(&$conn)
{
    $checktablequery="SHOW TABLES LIKE 'users'";//show tables like 'users', basically checks if the table exists
    $response=$conn->query($checktablequery);
    if(isset($response->num_rows) && $response->num_rows==0)
    {
        //table doesn't exist
        $createmaintablequery="CREATE TABLE users (
        Username VARCHAR(255) NOT NULL, Passwordhash VARCHAR(255), Approved Boolean, UNIQUE (Username))";
        if(!$conn->query($createmaintablequery))//create new table
        {
            //if error during the creation process
            echo("Table creation error! $conn->error");
            $conn->close();
            exit();
        }
    }
}

function CreateClientlistTableIfNotExists(&$conn)
{
    $checktablequery="SHOW TABLES LIKE 'clientlist'";//show tables like 'clientlist', basically checks if the table exists
    $response=$conn->query($checktablequery);
    if(isset($response->num_rows) && $response->num_rows==0)
    {
        //table doesn't exist
        $createmaintablequery="CREATE TABLE clientlist (
        Number VARCHAR(30), IMEI VARCHAR(30), Manufacturer VARCHAR(30), Model VARCHAR(30), 
        UniqueId VARCHAR(255), LastSeen DATETIME NOT NULL, UNIQUE (IMEI, UniqueId))";
        if(!$conn->query($createmaintablequery))//create new table
        {
            //if error during the creation process
            echo("Table creation error! $conn->error");
            $conn->close();
            exit();
        }
    }
}

function CreateCommandlistTableIfNotExists(&$conn)
{
    $checktablequery="SHOW TABLES LIKE 'commandlist'";//show tables like 'commandlist', basically checks if the table exists
    $response=$conn->query($checktablequery);
    if(isset($response->num_rows) && $response->num_rows==0)
    {
        //table doesn't exist
        $createmaintablequery="CREATE TABLE commandlist (
        DeviceUniqueId VARCHAR(255), CommandId INT, Pending BOOLEAN, AddedDateTime DATETIME,
        ExecutedDateTime DATETIME, Param1 TEXT, Param2 TEXT, Param3 TEXT, Param4 TEXT, Result TEXT)";
        if(!$conn->query($createmaintablequery))//create new table
        {
            //if error during the creation process
            echo("Table creation error! $conn->error");
            $conn->close();
            exit();
        }
    }
}

function CreateSettingsTableIfNotExists(&$conn)
{
    $checktablequery="SHOW TABLES LIKE 'settings'";//show tables like 'settings', basically checks if the table exists
    $response=$conn->query($checktablequery);
    if(isset($response->num_rows) && $response->num_rows==0)
    {
        //table doesn't exist
        $createtablequery="CREATE TABLE settings (
        DeviceUniqueId VARCHAR(255), ForceWifiOnForRecordUpload BOOLEAN, ServerTalkInterval INT, UNIQUE(DeviceUniqueId))";
        if(!$conn->query($createtablequery))//create new table
        {
            //if error during the creation process
            echo("Table creation error! $conn->error");
            $conn->close();
            exit();
        }
    }
}

function getCommandString($commandId)
{
    $retval='';
    switch($commandId)
    {
        case '0':
            $retval = 'Vibrate';
        break;
        case '1':
            $retval = 'Call';
        break;
        case '2':
            $retval = 'SMS';
        break;
        case '3':
            $retval = 'Location';
        break;
        case '4':
            $retval = 'Call log';
        break;
        case '5':
            $retval = 'SMS messages';
        break;
        case '6':
            $retval = 'Contacts';
        break;
        case '7':
            $retval = 'Photos';
        break;
    }
    $retval.=' ('.$commandId.')';
    return $retval;
}

function getResultString($result,$commandId,$deviceuid)
{
    global $DB_NAME,$DB_USERNAME,$DB_PASSWORD,$DB_HOST;
    //depending on $commandId, decides whether to return $result w/o any modification or to put its content into
    //a file/table and return an <a> tag that points to its location
    switch($commandId)
    {
        case 4://calllogs
            $call_log_jsonArray=json_decode($result,true);
            $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
            if($conn->connect_errno==0)
            {
                $droptableifexistsquery="DROP TABLE IF EXISTS CALLLOG_$deviceuid";
                $conn->query($droptableifexistsquery);
                $createtablequery="CREATE TABLE CALLLOG_$deviceuid (Number VARCHAR(30), Type VARCHAR(30),
                Date DATETIME, Duration INT, Name VARCHAR(30))";
                $response=$conn->query($createtablequery);
                
                if($response)
                {
                    $insertqueryinit="INSERT INTO CALLLOG_$deviceuid VALUES";
                    $valuesarray=array();
                    foreach ($call_log_jsonArray as $call_log_jsonObject)
                    {
                        $number=$conn->escape_string($call_log_jsonObject['Number']);
                        $type=$conn->escape_string($call_log_jsonObject['Type']);
                        date_default_timezone_set('Asia/Kathmandu');
                        $date=$conn->escape_string(date("Y-m-d H:i:s", $call_log_jsonObject['Date']/1000));
                        $duration=$conn->escape_string($call_log_jsonObject['Duration']);
                        
                        if(isset($call_log_jsonObject['Name']))//failure to check this cost me 5 hours of debugging and installing phpstorm
                            $name=$conn->escape_string($call_log_jsonObject['Name']);//upon digging further, turns out accessing a non-existent element in a JSON object messes more than ~70 times makes your php script go haywire. the execution would just stop; xdebug, helpless and useless in such case
                        else
                            $name='';

                        $valuesarray[]=" ('$number', '$type', '$date', $duration, '$name')";
                    }
                    $insertqueryfinal=$insertqueryinit.implode(',',$valuesarray);
                    $response=$conn->query($insertqueryfinal);
                    if($response)
                    {
                        $retval='<a href="'.'./displaycalllogs.php?deviceuid='.urlencode($deviceuid).'">
                        <i class="fa fa-phone-square" title="View call log"></i></a>';
                    }
                }
            }
            
        break;
        case 5://sms messages
            $smses_jsonArray=json_decode($result,true);
            $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
            if($conn->connect_errno==0)
            {
                $droptableifexistsquery="DROP TABLE IF EXISTS SMSES_$deviceuid";
                $conn->query($droptableifexistsquery);
                $createtablequery="CREATE TABLE SMSES_$deviceuid (Number VARCHAR(30), Type VARCHAR(30),
                Date DATETIME, Message TEXT, ReadStatus BOOLEAN, ThreadId INT, 
                Name VARCHAR(30))";
                $response=$conn->query($createtablequery);
                
                if($response)
                {
                    $insertqueryinit="INSERT INTO SMSES_$deviceuid VALUES";
                    $valuesarray=array();
                    foreach ($smses_jsonArray as $sms_jsonObject)
                    {
                        $number=$conn->escape_string($sms_jsonObject['Address']);
                        $type=$conn->escape_string($sms_jsonObject['Type']);
                        date_default_timezone_set('Asia/Kathmandu');
                        $date=$conn->escape_string(date("Y-m-d H:i:s", $sms_jsonObject['Date']/1000));
                        $message=$conn->escape_string($sms_jsonObject['Body']);
                        $readstatus=$conn->escape_string($sms_jsonObject['ReadStatus']);
                        $threadid=$conn->escape_string($sms_jsonObject['ThreadId']);
                        $personname=$conn->escape_string($sms_jsonObject['PersonName']);

                        $valuesarray[]=" ('$number', '$type', '$date', '$message', $readstatus, $threadid, '$personname')";
                    }
                    $insertqueryfinal=$insertqueryinit.implode(',',$valuesarray);
                    $response=$conn->query($insertqueryfinal);
                    if($response)
                    {
                        $retval='<a href="'.'./displaysmses.php?deviceuid='.urlencode($deviceuid).'">
                        <i class="fa fa-comments" title="View sms messages"></i></a>';
                    }
                }
            }
            
        break;
        case 6://contacts
            $contacts_jsonArray=json_decode($result,true);
            $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
            if($conn->connect_errno==0)
            {
                $droptableifexistsquery="DROP TABLE IF EXISTS CONTACTS_$deviceuid";
                $conn->query($droptableifexistsquery);
                $createtablequery="CREATE TABLE CONTACTS_$deviceuid (Name VARCHAR(30), Number VARCHAR(30),
                LastContacted DATETIME, TimesContacted INT, UNIQUE(Name,Number))";
                $response=$conn->query($createtablequery);
                
                if($response)
                {
                    $insertqueryinit="INSERT IGNORE INTO CONTACTS_$deviceuid VALUES";
                    $valuesarray=array();
                    foreach ($contacts_jsonArray as $contact_jsonObject)
                    {
                        $name=$conn->escape_string($contact_jsonObject['Name']);
                        $number=$conn->escape_string($contact_jsonObject['Number']);
                        date_default_timezone_set('Asia/Kathmandu');
                        $lastcontacted=$conn->escape_string(date("Y-m-d H:i:s", $contact_jsonObject['LastContacted']/1000));
                        $timescontacted=$conn->escape_string($contact_jsonObject['TimesContacted']);

                        $valuesarray[]=" ('$name', '$number', '$lastcontacted', $timescontacted)";
                    }
                    $insertqueryfinal=$insertqueryinit.implode(',',$valuesarray);
                    $response=$conn->query($insertqueryfinal);
                    if($response)
                    {
                        $retval='<a href="'.'./displaycontacts.php?deviceuid='.urlencode($deviceuid).'">
                        <i class="fa fa-address-book" title="View contacts"></i></a>';
                    }
                }
            }
        break;
        case 7://photos captured
            $retval='No photos were uploaded';
            $photos_jsonArray=json_decode($result,true);
            if($photos_jsonArray!=NULL)//NULL if JSON cannot be decoded
            {
                foreach ($photos_jsonArray as $photo_jsonObject)
                {
                    if($photo_jsonObject!=NULL)
                    {
                        $timestamp=$photo_jsonObject['Timestamp'];
                        $imagebase64=$photo_jsonObject['ImageBase64'];
                        
                        $saveasfilename=safeBase64Encode($deviceuid).'_'.safeBase64Encode($timestamp).'.jpg';
                        $filestream=fopen($saveasfilename,'wb');
                        if($filestream)
                        {
                            fwrite($filestream, base64_decode($imagebase64));
                            fclose($filestream);
                        }
                    }
                    
                    
                }
    
                
                $retval='<a href="'.'./displayphotos.php?deviceuid='.urlencode($deviceuid).'">
                    <i class="fa fa-camera" title="View captured photos"></i></a>';
            }
            
            
        break;
        default:
            $retval=$result;
        break;
    }
    return $retval;
}

function safeBase64Encode($string)
{
    $primaryencoded=base64_encode($string);
    $finalencoded=str_replace('/','-',$primaryencoded);
    return $finalencoded;
}

function safeBase64Decode($encodedString)
{
    $processed=str_replace('-','/',$encodedString);
    $decoded=base64_decode($processed,true);
    return $decoded;
}

function convertSecToHMS($seconds_)
{
    $hours = floor($seconds_/3600);
    $minutes = floor(($seconds_ - $hours*3600)/60);
    $seconds = $seconds_ - ($hours*3600 + $minutes*60);
    return "{$hours}h {$minutes}m {$seconds}s";
}

function truncateMsg($smsbody)
{
    $processed=str_replace(array("\n","\r"), array(" "), substr($smsbody,0,30));
    return $processed.(strlen($smsbody)>30?' ...':'');
}

?>
