<?php

	require_once 'encryption.php';

	class DB {

		protected $db;
		public $data;

		public function __construct(Connection $db) {
			$this->db = $db;
		}

		public function getResponse() {
			return $this->data;
		}


		public function addData($data = array()) {
			if(!empty($data['var']) && !empty($data['val']) && !empty($data['corresponder'])) {


				$exists = $this->db->exe("SELECT * FROM store WHERE corresponder=? AND var=?", [$data['corresponder'], $data['var']])->fetch();
				if($exists['corresponder'] == $data['corresponder'] && $exists['var'] == $data['var']) {
					$this->data = array('error'=>'var_exists_on_corresponder');
				} else {
					$enc = new EncryptionHelper($data['val']);
					$enc->protect();
					$value = $enc->getResponse()['hashed'];
					$var = $data['var'];
					if($this->db->exe("INSERT INTO store (id, corresponder, var, val) VALUES (?, ?, ?, ?)", ['', $data['corresponder'], $var, $value])) {
						$this->data = array('success'=>'stored_data');
					}
				}			
			} else {
				$this->data = array('error'=>'empty_data');
			}
		}

		public function getData() {

		}

		public function createNewWord($word = array()) {
			if(!empty($word['word']) && !empty($severity)) {
				$word = $word['word'];
				$severity = $word['severity'];
				$exists = $this->db->exe("SELECT * FROM words WHERE word=?", [$word])->fetch();
				if($exists['word'] == "" && $exists['word'] != $word) {
					if($this->db->exe("INSERT INTO words (id, word, severity) VALUES (?, ?, ?)", ['', $word, $severity])) {
						$this->data = array('success'=>'word_added');
					} else {
						$this->data = array('error'=>'word_creation_error');
					}
				} else {
					$this->data = array('error'=>'word_exists');
				}
			} else {
				$this->data = array('error'=>'word_empty');
			}
		}

		private function isJSON($string) {
 			json_decode($string);
 			return (json_last_error() == JSON_ERROR_NONE);
		}
	}
