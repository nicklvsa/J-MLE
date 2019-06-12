<?php 

	//for testing
	//header("Access-Control-Allow-Origin: *");

	require_once 'connection.php';
	require_once 'database.php';

	if(isset($_POST['store'])) {

		$connection = new Connection("mysql:host=SQL_HOST;dbname=SQL_DB;charset=utf8", "SQL_USER", "SQL_PASSWORD");
		$db = new DB($connection);

		$corresponder = $_POST['corresponder'];
		$var = $_POST['var'];
		$val = $_POST['val'];

		$db->addData(array(
			"var"=>$var,
			"val"=>$val,
			"corresponder"=>$corresponder
		));

		print_r(json_encode($db->getResponse()));
	}

?>