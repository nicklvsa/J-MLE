package com.nicksdesk.mle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class Parser {

	/*
	 * Parser can technically parse groovy code but not needed
	 */
	
	public static HashMap<Object, HashMap<Object, Object>> mles = new HashMap<Object, HashMap<Object, Object>>();
	private static HashMap<Object, Object> corresponder = new HashMap<Object, Object>();
	private static HashMap<Object, Object> vars = new HashMap<Object, Object>();
	private static HashMap<Object, Boolean> logicResponses = new HashMap<Object, Boolean>();
	private static HashMap<Object, ArrayList<String>> controlHeaders = new HashMap<Object, ArrayList<String>>();
	private static HashMap<Object, Object> headerIdController = new HashMap<Object, Object>();
	
	/*
	 * TODO: add update function
	 */
	
	public static String getCorresponderData(Object corresponder) {
		return String.valueOf(mles.get(corresponder));
	}
	
	public static String getVar(Object corresponder, Object var) {
		return String.valueOf(mles.get(corresponder).get((!(var instanceof Integer)) ? ("\""+var+"\"").trim() : String.valueOf(var)));
	}
	
	public static HashMap<Object, ArrayList<String>> getRawControlHeaders() {
		return controlHeaders;
	}
	
	public static Object getControlHeader(Object key) {
		if(controlHeaders.containsKey(key)) {
			return controlHeaders.get(key);
		} else {
			return "Error: Parser - Control Header does not exist!";
		}
	}
	
	public static String getRaw() {
		return mles.toString();
	}
	
	public String parseBody(String body) {
		if(!body.isEmpty()) {
			List<String> lines = Arrays.asList(body.split("\\n"));
			for(String line : lines) {
				if(!line.contains("correspond")) {
					if(!line.contains("var")) {
						if(!line.contains("delete")) {
							if(!line.contains("<$groovy")) {
								if(!line.contains("@logic")) {
									if(!line.contains("@control")) {
										if(!line.contains("@store")) {
											continue;
										} else {
											String varToStore = line.substring(line.indexOf("@store(")+7, line.indexOf(")")).trim();
											if(corresponder.containsKey("corresponder")) {
												String corresponderStr = String.valueOf(corresponder.get("corresponder"));
												if(vars.containsKey(varToStore)) {
													String varStr = (String) vars.get(varToStore);
													if(Utils.store(corresponderStr, varToStore, varStr)) {
														Utils.log("Successfully stored: " + varToStore + " to the db!", 0);
													} else {
														Utils.log("Unable to store! Are you sure this cloud variable is not used by the current corresponder?", 1);
													}
												} else {
													Utils.log("no_var", 1);
												}
											} else {
												Utils.log("no_corresponder", 1);
											}
										}
									} else {
										String controller = line.substring(line.indexOf("@control(")+9, line.indexOf(")")).trim();
										String controllerId = line.substring(line.indexOf(controller)+controller.length(), line.indexOf(";")).replaceAll("\"", " ").replaceAll("\\)", " ").replaceAll(":", " ").trim();
										headerIdController.put(controllerId, controller);
										List<String> headers = new ArrayList<String>(Arrays.asList(controller.split(",")));
										//Keep ALL selector for now until I fix it (Ignoring case won't work since parser is forced to read as lower case)
										controlHeaders.put("*", (ArrayList<String>) headers);
									}
								} else {
									String raw = body.substring(body.indexOf("<@logic")+7, body.indexOf("@>"));
									List<String> logicCode = Arrays.asList(raw.split("\\n"));
									for(String logic : logicCode) {
										logic = logic.trim();
										if(logic.startsWith("supposing") && !logic.contains("modify")) {
											String supposeLogicController = logic.substring(logic.indexOf("supposing(")+12, logic.indexOf(")"));
											String supposeLogicId = logic.substring(logic.indexOf(supposeLogicController)+supposeLogicController.length(), logic.indexOf("then")).replaceAll("\\)", " ").trim();
											System.out.println("Correspond: " + supposeLogicController + " " + supposeLogicId);
											String supposeExecutes = body.substring(body.indexOf("then{")+5, body.indexOf("}")).trim();
											List<String> sCode = Arrays.asList(supposeExecutes.split("\\n"));
											for(String code : sCode) {
												/*
												 * Parse code inside suppose statement
												 */
												
											}
										} 
										if(logic.startsWith("modify")) {
											if(logic.startsWith("modify~")) {
												if(logic.contains("supposing")) {
													String controlledBy = logic.substring(logic.indexOf("modify~")+7, logic.indexOf("(")).trim();
													String modifier = logic.substring(logic.indexOf(controlledBy)+controlledBy.length()+2, logic.indexOf(")")).trim();
													if(headerIdController.containsKey(controlledBy.replaceAll("\"", " ").trim())) {
														System.out.println(controlledBy + " BY: " + headerIdController.get(controlledBy.replaceAll("\"", " ").trim()));
													} else {
														/*
														 * Controller set but key never init by @control
														 */
													}
													if(modifier.contains("\"")) {
														String fullLogic = logic.substring(logic.indexOf(modifier)+modifier.length()+2, logic.indexOf(";")+1).replaceAll("supposing", " ").trim();
														String logicController = fullLogic.substring(fullLogic.indexOf("("), fullLogic.indexOf("<")).trim();
														String newValue = fullLogic.substring(fullLogic.indexOf("<-")+2, fullLogic.indexOf(";")).trim();
														if(vars.containsKey(modifier)) {
															if(this.parseLogic(logicController)) {
																vars.replace(modifier, vars.get(modifier), newValue.replaceAll("\"", " ").trim());
																logicResponses.put(modifier, true);
															} else {
																logicResponses.put(modifier, false);
															}
														} else {
															return "Error: Parser - Variable does not exist!";
														}
													} else if(Reader.isNumeric(modifier)) {
														String fullLogic = logic.substring(logic.indexOf(modifier)+modifier.length(), logic.indexOf(";")+1).replaceAll("supposing", " ").trim();
														String logicController = fullLogic.substring(fullLogic.indexOf("("), fullLogic.indexOf("<")).trim();
														String newValue = fullLogic.substring(fullLogic.indexOf("<-")+2, fullLogic.indexOf(";")).trim();
														if(vars.containsKey(modifier)) {
															if(this.parseLogic(logicController)) {
																vars.replace(modifier, vars.get(modifier), newValue.trim());
																logicResponses.put(modifier, true);
															} else {
																logicResponses.put(modifier, false);
															}
														} else {
															return "Error: Parser - Variable does not exist!";
														}
													} else {
														return "Error: Parser - Invalid variable type declared (can only be INT or STRING)";
													}
												} else {
													String modifier = logic.substring(logic.indexOf("modify(")+9, logic.indexOf(")")).trim();
													if(modifier.contains("\"")) {
														String fullLogic = logic.substring(logic.indexOf(modifier)+modifier.length(), logic.indexOf(";")+1).trim();
														String newValue = fullLogic.substring(fullLogic.indexOf("<-")+2, fullLogic.indexOf(";")).trim();
														if(vars.containsKey(modifier)) {
															vars.replace(modifier, vars.get(modifier), newValue.trim());
														} else {
															return "Error: Parser - Variable does not exist!";
														}
													} else if(Reader.isNumeric(modifier)) {
														
													} else {
														return "Error: Parser - Invalid variable type declared (can only be INT or STRING)";
													}
												}
											} else {
												if(logic.contains("supposing")) {
													String modifier = logic.substring(logic.indexOf("modify(")+9, logic.indexOf(")")).trim();
													if(modifier.contains("\"")) {
														String fullLogic = logic.substring(logic.indexOf(modifier)+modifier.length(), logic.indexOf(";")+1).replaceAll("supposing", " ").trim();
														String logicController = fullLogic.substring(fullLogic.indexOf("("), fullLogic.indexOf("<")).trim();
														String newValue = fullLogic.substring(fullLogic.indexOf("<-")+2, fullLogic.indexOf(";")).trim();
														if(vars.containsKey(modifier)) {
															if(this.parseLogic(logicController)) {
																vars.replace(modifier, vars.get(modifier), newValue.replaceAll("\"", " ").trim());
																logicResponses.put(modifier, true);
															} else {
																logicResponses.put(modifier, false);
															}
														} else {
															return "Error: Parser - Variable does not exist!";
														}
													} else if(Reader.isNumeric(modifier)) {
														String fullLogic = logic.substring(logic.indexOf(modifier)+modifier.length(), logic.indexOf(";")+1).replaceAll("supposing", " ").trim();
														String logicController = fullLogic.substring(fullLogic.indexOf("("), fullLogic.indexOf("<")).trim();
														String newValue = fullLogic.substring(fullLogic.indexOf("<-")+2, fullLogic.indexOf(";")).trim();
														if(vars.containsKey(modifier)) {
															if(this.parseLogic(logicController)) {
																vars.replace(modifier, vars.get(modifier), newValue.trim());
																logicResponses.put(modifier, true);
															} else {
																logicResponses.put(modifier, false);
															}
														} else {
															return "Error: Parser - Variable does not exist!";
														}
													} else {
														return "Error: Parser - Invalid variable type declared (can only be INT or STRING)";
													}
												} else {
													String modifier = logic.substring(logic.indexOf("modify(")+9, logic.indexOf(")")).trim();
													if(modifier.contains("\"")) {
														String fullLogic = logic.substring(logic.indexOf(modifier)+modifier.length(), logic.indexOf(";")+1).trim();
														String newValue = fullLogic.substring(fullLogic.indexOf("<-")+2, fullLogic.indexOf(";")).trim();
														if(vars.containsKey(modifier)) {
															vars.replace(modifier, vars.get(modifier), newValue.trim());
														} else {
															return "Error: Parser - Variable does not exist!";
														}
													} else if(Reader.isNumeric(modifier)) {
														
													} else {
														return "Error: Parser - Invalid variable type declared (can only be INT or STRING)";
													}
												}
											}
											
										}
									}
								}
							} else {
								StringBuilder content = new StringBuilder();
								String raw = body.substring(body.indexOf("<$groovy")+8, body.indexOf("$>"));
								List<String> jreCode = Arrays.asList(raw.split("\\n"));
								for(String code : jreCode) {
									if(code.contains("<@")) {
										String var = code.substring(code.indexOf("<@")+2, code.indexOf(">")).trim();
										if(!var.isEmpty()) {
											if(!Reader.isNumeric(var)) {
												if(vars.containsKey("\""+var+"\"")) {
													code = code.replaceAll(var, String.valueOf(vars.get("\""+var+"\"").toString().replaceAll("\"", "").trim())).replaceAll("<", "").replaceAll(">", "").replaceAll("@", "").trim();
												} else {
													return "Error: Parser - Variable does not exist!";
												}
											} else if(Reader.isNumeric(var)) {
												if(vars.containsKey(var)) {
													code = code.replaceAll(var, String.valueOf(vars.get(var).toString().replaceAll("\"", "").trim())).replaceAll("<", "").replaceAll(">", "").replaceAll("@", "").trim();
												} else {
													return "Error: Parser - Variable does not exist!";
												}
											}
										} else {
											return "Error: Parser - Variable Empty!";
										}
									}
									content.append(code);
								}
								
								if(!content.toString().isEmpty()) {
									Binding bind = new Binding();
									GroovyShell shell = new GroovyShell(bind);
									Object returnVal = shell.evaluate(content.toString());
									if(returnVal == null) continue;
									return returnVal.toString();
								} else {
									return "Error: Parser - Content empty to parse JRE code!";
								}
							}
						} else {
							String toDelete = line.substring(line.indexOf("delete(")+7, line.indexOf(")"));
							if(!toDelete.isEmpty()) {
								if(vars.containsKey(toDelete)) {
									vars.remove(toDelete);
								} else {
									return "Error: Parser - Var does not exist to delete!";
								}
							} else {
								return "Error: Parser - Delete var is empty!";
							}
						}
					} else {
						String var = line.substring(line.indexOf("var(")+4, line.indexOf(",")).trim();
						String varValue = line.substring(line.indexOf("var("+var)+(5+var.length()), line.indexOf(")")).trim();
						String varType = line.substring(line.indexOf(varValue)+varValue.length()+1, line.indexOf("&")).replace("key as", "").trim();
						String valueType = line.substring(line.indexOf(line.substring(line.indexOf(varValue)+varValue.length()+1, line.indexOf("&")))+line.substring(line.indexOf(varValue)+varValue.length()+1, line.indexOf("&")).length()+1, line.indexOf(";")).replace("val as", "").replace(",", "").trim();
						//System.out.println(var + " -> ("+varType+") :" + varValue + " -> ("+valueType+")");
						if(!var.isEmpty() && !varValue.isEmpty() && !varType.isEmpty() && !valueType.isEmpty()) {
							switch(varType) {
								case "string":
									if(var.contains("\"") && !Reader.isNumeric(var)) {
										switch(valueType) {
											case "string":
												if(varValue.contains("\"") && !Reader.isNumeric(varValue)) {
													if(!vars.containsKey(var)) {
														vars.put(var, varValue);
													} else {
														return "Parser: Error - Var already exists!";
													}
												} else {
													return "Parser: Error - Var value type mismatch - " + varValue;
												}
											continue;
											case "int":
												if(!varValue.contains("\"") && Reader.isNumeric(varValue)) {
													if(!vars.containsKey(var)) {
														vars.put(var, varValue);
													} else {
														return "Parser: Error - Var already exists!";
													}
												} else {
													return "Parser: Error - Var value type mismatch - " + varValue;
												}
											continue;
											default:
											return "Parser: Error - Invalid var value type!";
										}
									} else {
										return "Parser: Error - Var type mismatch!";
									}
								case "int":
									if(!var.contains("\"") && Reader.isNumeric(var)) {
										switch(valueType) {
											case "string":
												if(varValue.contains("\"") && !Reader.isNumeric(varValue)) {
													if(!vars.containsKey(var)) {
														vars.put(var, varValue);
													} else {
														return "Parser: Error - Var already exists!";
													}
												} else {
													return "Parser: Error - Var value type mismatch";
												}
											continue;
											case "int":
												if(!varValue.contains("\"") && Reader.isNumeric(varValue)) {
													if(!vars.containsKey(var)) {
														vars.put(var, varValue);
													} else {
														return "Parser: Error - Var already exists!";
													}
												} else {
													return "Parser: Error - Var value type mismatch - " + varValue;
												}
											continue;
											default:
											return "Parser: Error - Invalid var value type!";
										}
									} else {
										return "Parser: Error - Var type mismatch!";
									}
								default:
								return "Parser: Error - Invalid var type!";
							}
							
						} else {
							return "Parser: Error - Variable field empty!";
						}
					}
				} else {
					String correspond = line.substring(line.indexOf("correspond(")+11, line.indexOf(")"));
					String corresponderType = line.substring(line.indexOf("correspond("+correspond+")")+(correspond.length() + 12), line.indexOf(";")).replace("as", "").trim();
					if(!correspond.isEmpty()) {
						switch(corresponderType) {
							case "string":
								if(correspond.contains("\"") && !Reader.isNumeric(correspond)) {
									correspond = correspond.replaceAll("\"", "").trim();
									corresponder.put("corresponder", correspond);
								} else {
									return "Error: Parser - Corresponder type mismatch!";
								}
							continue;
							case "int":
								if(!correspond.contains("\"") && Reader.isNumeric(correspond)) {
									corresponder.put("corresponder", correspond);
								} else {
									return "Error: Parser - Corresponder type mismatch!";
								}
							continue;
							default:
							return "Error: Parser - Invalid corresponder type";
						}
					} else {
						return "Error: Parser - Corresponder empty!";
					}
				}
			}
		} else {
			return "Error: Parser - Body empty!";
		}
		mles.put(corresponder.get("corresponder"), vars);
		return mles.toString();
	}
	
	private boolean parseLogic(String logic) {
		if(logic.contains("==")) {
			String left = logic.substring(logic.indexOf("(")+1, logic.indexOf("==")).trim();
			String right = logic.substring(logic.indexOf("==")+2, logic.indexOf(")")).trim();
			if(Reader.isNumeric(left) && Reader.isNumeric(right)) {
				if(Integer.parseInt(left) == Integer.parseInt(right)) {
					return true;
				} else {
					return false;
				}
			} else if(Reader.isNumeric(left) && !Reader.isNumeric(right)) {
				return false;
			} else if(!Reader.isNumeric(left) && Reader.isNumeric(right)) {
				return false;
			} else if(!Reader.isNumeric(left) && !Reader.isNumeric(right)) {
				if(String.valueOf(left).equalsIgnoreCase(String.valueOf(right))) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if(logic.contains("<=")) {
			String left = logic.substring(logic.indexOf("(")+1, logic.indexOf("<=")).trim();
			String right = logic.substring(logic.indexOf("<=")+2, logic.indexOf(")")).trim();
			if(Reader.isNumeric(left) && Reader.isNumeric(right)) {
				if(Integer.parseInt(left) <= Integer.parseInt(right)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if(logic.contains(">=")) {
			String left = logic.substring(logic.indexOf("(")+1, logic.indexOf(">=")).trim();
			String right = logic.substring(logic.indexOf(">=")+2, logic.indexOf(")")).trim();
			if(Reader.isNumeric(left) && Reader.isNumeric(right)) {
				if(Integer.parseInt(left) >= Integer.parseInt(right)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if(logic.contains("<")) {
			String left = logic.substring(logic.indexOf("(")+1, logic.indexOf("<")).trim();
			String right = logic.substring(logic.indexOf("<")+1, logic.indexOf(")")).trim();
			if(Reader.isNumeric(left) && Reader.isNumeric(right)) {
				if(Integer.parseInt(left) < Integer.parseInt(right)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if(logic.contains(">")) {
			String left = logic.substring(logic.indexOf("(")+1, logic.indexOf(">")).trim();
			String right = logic.substring(logic.indexOf(">")+1, logic.indexOf(")")).trim();
			if(Reader.isNumeric(left) && Reader.isNumeric(right)) {
				if(Integer.parseInt(left) > Integer.parseInt(right)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}