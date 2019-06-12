<?php 

	class Connection extends PDO {
		
		public function __construct($in, $username, $password, $options = []) {
			$default_options = [
				PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
				PDO::ATTR_EMULATE_PREPARES => false,
				PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
			];
			$options = array_replace($default_options, $options);
			parent::__construct($in, $username, $password, $options);
		}

		public function exe($query, $arguments) {
			if(!$arguments) {
				return $this->query($query);
			}
			$result = $this->prepare($query);
			$result->execute($arguments);
			return $result;
		}
	}