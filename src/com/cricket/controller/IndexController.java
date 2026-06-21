package com.cricket.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.cricket.config.DataSourceConfig;
import com.cricket.model.BattingCard;
import com.cricket.model.BowlingCard;
import com.cricket.model.Configuration;
import com.cricket.model.DaySession;
import com.cricket.model.Event;
import com.cricket.model.EventFile;
import com.cricket.model.FallOfWicket;
import com.cricket.model.Inning;
import com.cricket.model.Match;
import com.cricket.model.MatchAllData;
import com.cricket.model.MatchFinishTime;
import com.cricket.model.Partnership;
import com.cricket.model.Player;
import com.cricket.model.Review;
import com.cricket.model.Setup;
import com.cricket.model.Shot;
import com.cricket.model.Speed;
import com.cricket.model.Spell;
import com.cricket.model.Wagon;
import com.cricket.service.CricketService;
import com.cricket.util.CricketFunctions;
import com.cricket.util.CricketUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class IndexController
{
	@Autowired
	CricketService cricketService;

	public String expiry_date = "2026-12-31";
	public String current_date = "";
	public String error_message = "";
	public Speed session_speed = new Speed();
	public MatchAllData session_match = new MatchAllData();
	public Configuration session_config = new Configuration();
	public boolean backMatchData = true;
	public ObjectMapper objectMapper = new ObjectMapper();
	public String masterCricketDirectory = CricketUtil.CRICKET_DIRECTORY;
	public File directoryFile = new File(CricketUtil.SPORTS_DIRECTORY + "CricketDirectory.txt");
	//public Match last_match_data = new Match();

	@RequestMapping(value = {"/setup"}, method = RequestMethod.POST)
	public String setupPage(ModelMap model) throws ParseException 
	{
		model.addAttribute("match_files", new File(masterCricketDirectory + CricketUtil.MATCHES_DIRECTORY).listFiles(new FileFilter() {
			@Override
		    public boolean accept(File pathname) {
		        return pathname.isFile();
		    }
		}));
		model.addAttribute("teams", cricketService.getTeams());
		model.addAttribute("grounds", cricketService.getGrounds());
		model.addAttribute("licence_expiry_message",
			"Software licence expires on " + new SimpleDateFormat("E, dd MMM yyyy").format(
			new SimpleDateFormat("yyyy-MM-dd").parse(expiry_date)));
		
		return "setup";
	}

	@RequestMapping(value = {"/","/match"}, method = {RequestMethod.POST,RequestMethod.GET})
	public String cricketMatchPage(ModelMap model)
		throws MalformedURLException, IOException, ParseException, URISyntaxException  
	{
		if(current_date == null || current_date.isEmpty()) {
			current_date = CricketFunctions.getOnlineCurrentDate();
		}
		if(current_date == null || current_date.isEmpty()) {
		
			model.addAttribute("error_message","You must be connected to the internet online");
			return "error";
		
		} else if(new SimpleDateFormat("yyyy-MM-dd").parse(expiry_date).before(new SimpleDateFormat("yyyy-MM-dd").parse(current_date))) {
			
			model.addAttribute("error_message","This software has expired");
			return "error";
			
		}else {
		
//	        PrintStream fileOut = new PrintStream(new FileOutputStream("C:\\Temp\\myAppLog.txt", true));
//	        System.setOut(fileOut);
//	        System.setErr(fileOut);			
			
			if(!directoryFile.exists()) {
				directoryFile.createNewFile();
				switchCricketDirectory("");
			} else {
				String whichDirectory = Files.readString(directoryFile.toPath());
				switchCricketDirectory(whichDirectory);
			}
			
			model.addAttribute("match_files", new File(masterCricketDirectory + CricketUtil.MATCHES_DIRECTORY).listFiles(new FileFilter() {
				@Override
			    public boolean accept(File pathname) {
			        return pathname.isFile();
			    }
			}));
			
			if(new File(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY).exists() == false) {
				new File(masterCricketDirectory, CricketUtil.BACK_UP_DIRECTORY).mkdirs();
			}
			if(new File(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY + CricketUtil.MATCHES_DIRECTORY).exists() == false) {
				new File(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY, CricketUtil.MATCHES_DIRECTORY).mkdirs();
			}
			if(new File(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY + CricketUtil.SETUP_DIRECTORY).exists() == false) {
				new File(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY, CricketUtil.SETUP_DIRECTORY).mkdirs();
			}
			if(new File(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY + CricketUtil.EVENT_DIRECTORY).exists() == false) {
				new File(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY, CricketUtil.EVENT_DIRECTORY).mkdirs();
			}
//			if(new File(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY + CricketUtil.INTERACTIVE_DIRECTORY).exists() == false) {
//				new File(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY, CricketUtil.INTERACTIVE_DIRECTORY).mkdirs();
//			}
			
			model.addAttribute("licence_expiry_message",
				"Software licence expires on " + new SimpleDateFormat("E, dd MMM yyyy").format(
				new SimpleDateFormat("yyyy-MM-dd").parse(expiry_date)));
			
			model.addAttribute("cricketMatchesDirectory", 
				session_config.getCricketDirectory() == null ? "" : session_config.getCricketDirectory().toLowerCase());			
			
			session_match = new MatchAllData();
			session_match.setMatch(new Match()); 
			session_match.setSetup(new Setup()); 
			session_match.setEventFile(new EventFile());
			session_speed = new Speed(0);
			backMatchData = true;
			
			return "index";
		}
	}
	
	public void switchCricketDirectory(String whichDirectory)throws IOException
	{
		whichDirectory = whichDirectory.trim().toLowerCase();
		String baseDirectory = CricketUtil.CRICKET_DIRECTORY;
		if(baseDirectory.endsWith("/") || baseDirectory.endsWith("\\")) {
			baseDirectory = baseDirectory.substring(0, baseDirectory.length() - 1);
		}
		if(whichDirectory.isEmpty()) {
			masterCricketDirectory = CricketUtil.CRICKET_DIRECTORY;
			session_config.setCricketDirectory("");
			DataSourceConfig.switchDatabase("");
		}
		else
		{
			String formattedDirectory = StringUtils.capitalize(whichDirectory);
			masterCricketDirectory = baseDirectory + formattedDirectory + "/";
			session_config.setCricketDirectory(formattedDirectory);
			DataSourceConfig.switchDatabase(formattedDirectory);
		}
		if(!directoryFile.exists()) {
			directoryFile.createNewFile();
		}
		try(FileWriter writer = new FileWriter(directoryFile, false)) {
			writer.write(whichDirectory);
		}
	}
	
	@RequestMapping(value = {"/upload_match_setup_data", "/reset_and_upload_match_setup_data", 
		"/upload_shot_data", "/upload_wagon_data", "/save_cricket_directory"}, method={RequestMethod.POST, RequestMethod.GET})    
	public @ResponseBody String uploadFormDataToSessionObjects(HttpServletRequest request) // (MultipartHttpServletRequest request) 
		throws IllegalAccessException, InvocationTargetException, IOException, URISyntaxException, JAXBException
	{
		if (request.getRequestURI().contains("save_cricket_directory")) {
			String formatData = request.getParameter("formatData");
			if(formatData != null) {
				switchCricketDirectory(formatData);
			}
			return "";
		}
		
		boolean reset_all_variables = false;
   		
		if (request.getRequestURI().contains("upload_match_setup_data") || request.getRequestURI().contains("reset_and_upload_match_setup_data")) {
			
			List<Player> home_squad = new ArrayList<Player>(); List<Player> away_squad = new ArrayList<Player>();
			List<Player> home_substitutes = new ArrayList<Player>(); List<Player> away_substitutes = new ArrayList<Player>();
			List<Inning> inns = new ArrayList<Inning>();
			List<BattingCard> battingCardList = new ArrayList<BattingCard>();
			BattingCard this_batting_card = null;
	   		int max_inns = 0;
	   		List<String> setupHomeTeam = new ArrayList<String>(), setupAwayTeam = new ArrayList<String>();

	   		if(request.getRequestURI().contains("reset_and_upload_match_setup_data")) {
				reset_all_variables = true;
			} else if(request.getRequestURI().contains("upload_match_setup_data")) {
				for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
					if(entry.getKey().equalsIgnoreCase("select_existing_cricket_matches") && entry.getValue()[0].equalsIgnoreCase("new_match")) {
						reset_all_variables = true;
						break;
					}
				}
			}
			
			if(reset_all_variables == true) {
				session_match = new MatchAllData(); 
				session_match.setMatch(new Match());
				session_match.setSetup(new Setup());
				session_match.setEventFile(new EventFile());
				session_match.getEventFile().setEvents(new ArrayList<Event>());
	   			for(int i=1; i<=CricketUtil.LIMITED_OVER_MAXIMUM_INNINGS; i++)
	   				inns.add(new Inning());
			} else {
				if(session_match.getMatch() == null) {
					session_match.setMatch(new Match());
				}
				if(session_match.getSetup() == null) {
					session_match.setSetup(new Setup());
				}
				if(session_match.getEventFile() == null) {
					session_match.setEventFile(new EventFile());
					session_match.getEventFile().setEvents(new ArrayList<Event>());
				}
				if(session_match.getMatch().getInning() != null && session_match.getMatch().getInning().size() > 0) {
					for(Inning inn : session_match.getMatch().getInning()) {
		   				inns.add(inn);
					}
				} else {
		   			for(int i=1; i<=CricketUtil.LIMITED_OVER_MAXIMUM_INNINGS; i++) 
		   				inns.add(new Inning());
				}
			}

			BeanWrapperImpl matchWrapper = new BeanWrapperImpl(session_match.getMatch());
	   		BeanWrapperImpl setupWrapper = new BeanWrapperImpl(session_match.getSetup());
			
			session_match.getSetup().setMatchDataUpdate(CricketUtil.START);
			for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
				if (entry.getValue()[0] == null || entry.getValue()[0].trim().isEmpty() || entry.getValue()[0].equalsIgnoreCase("undefined")) {
					continue;
				}
				if(entry.getKey().toUpperCase().startsWith(CricketUtil.HOME + CricketUtil.PLAYER + "_") 
					|| entry.getKey().toUpperCase().startsWith(CricketUtil.AWAY + CricketUtil.PLAYER + "_")) {
   					if(entry.getKey().split("_")[0].equalsIgnoreCase(CricketUtil.HOME + CricketUtil.PLAYER)) {
						setupHomeTeam.add(entry.getKey().split("_")[1] + "|" + Integer.parseInt(entry.getValue()[0]));
   						switch (Integer.parseInt(entry.getKey().split("_")[1])) {
   						case 1: case 2: case 3: case 4: case 5: case 6:
   						case 7: case 8: case 9: case 10: case 11:
   		   					home_squad.add(new Player(Integer.parseInt(entry.getValue()[0]), 
   		   						Integer.parseInt(entry.getKey().split("_")[1])));
   							break;
   						default:
   		   					home_substitutes.add(new Player(Integer.parseInt(entry.getValue()[0]), 
   		   						Integer.parseInt(entry.getKey().split("_")[1])));
   							break;
   						}
   					} else if(entry.getKey().split("_")[0].equalsIgnoreCase(CricketUtil.AWAY + CricketUtil.PLAYER)) {
   						setupAwayTeam.add(entry.getKey().split("_")[1] + "|" + Integer.parseInt(entry.getValue()[0]));
   						switch (Integer.parseInt(entry.getKey().split("_")[1])) {
   						case 1: case 2: case 3: case 4: case 5: case 6:
   						case 7: case 8: case 9: case 10: case 11:
   		   					away_squad.add(new Player(Integer.parseInt(entry.getValue()[0]), 
   		   						Integer.parseInt(entry.getKey().split("_")[1])));
   							break;
   						default:
   		   					away_substitutes.add(new Player(Integer.parseInt(entry.getValue()[0]), 
   		   						Integer.parseInt(entry.getKey().split("_")[1])));
   							break;
   						}
   					}
   					
	   			} else if (entry.getKey().toUpperCase().contains("MATCHFILENAME")) {
	   				matchWrapper.setPropertyValue(entry.getKey(), entry.getValue()[0].substring(0, entry.getValue()[0].indexOf('.')) + ".json");
	   			} else {
	   				if(setupWrapper.isWritableProperty(entry.getKey())) {
	   					setupWrapper.setPropertyValue(entry.getKey(), entry.getValue()[0]);
	   				}
	   			}
	   		}
			for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
	   			if(entry.getKey().toUpperCase().contains(CricketUtil.HOME + CricketUtil.CAPTAIN + CricketUtil.WICKET_KEEPER.replace("_", "")) 
	   					|| entry.getKey().toUpperCase().contains(CricketUtil.AWAY + CricketUtil.CAPTAIN + CricketUtil.WICKET_KEEPER.replace("_", ""))) {
	   				if(entry.getKey().split("_")[0].equalsIgnoreCase(CricketUtil.HOME + CricketUtil.CAPTAIN + CricketUtil.WICKET_KEEPER.replace("_", ""))) {
	   					for(Player plyr:home_squad) {
	   						if(plyr.getPlayerPosition() == Integer.parseInt(entry.getKey().split("_")[1])) {
	   							plyr.setCaptainWicketKeeper(entry.getValue()[0]);
	   						}
	   					}
	   					for(Player plyr:home_substitutes) {
	   						if(plyr.getPlayerPosition() == Integer.parseInt(entry.getKey().split("_")[1])) {
	   							plyr.setCaptainWicketKeeper(entry.getValue()[0]);
	   						}
	   					}
	   				} else if(entry.getKey().split("_")[0].equalsIgnoreCase(CricketUtil.AWAY + CricketUtil.CAPTAIN + CricketUtil.WICKET_KEEPER.replace("_", ""))) {
	   					for(Player plyr:away_squad) {
	   						if(plyr.getPlayerPosition() == Integer.parseInt(entry.getKey().split("_")[1])) {
	   							plyr.setCaptainWicketKeeper(entry.getValue()[0]);
	   						}
	   					}
	   					for(Player plyr:away_substitutes) {
	   						if(plyr.getPlayerPosition() == Integer.parseInt(entry.getKey().split("_")[1])) {
	   							plyr.setCaptainWicketKeeper(entry.getValue()[0]);
	   						}
	   					}
   					}
	   			}
	   		}
			
			session_match.getSetup().setSetupHomeTeam(String.join(",", setupHomeTeam));
			session_match.getSetup().setSetupAwayTeam(String.join(",", setupAwayTeam));
			session_match.getSetup().setHomeSquad(home_squad);
			session_match.getSetup().setAwaySquad(away_squad);
			
			Collections.sort(session_match.getSetup().getHomeSquad());
			Collections.sort(session_match.getSetup().getAwaySquad());

			session_match.getSetup().setHomeSubstitutes(home_substitutes);
			session_match.getSetup().setAwaySubstitutes(away_substitutes);

			Collections.sort(session_match.getSetup().getHomeSubstitutes());
			Collections.sort(session_match.getSetup().getAwaySubstitutes());
			
			session_match.getSetup().setHomeOtherSquad(CricketFunctions.getPlayersFromDB(
				cricketService, CricketUtil.HOME, session_match));
			session_match.getSetup().setAwayOtherSquad(CricketFunctions.getPlayersFromDB(
				cricketService, CricketUtil.AWAY, session_match));
			
			if (session_match.getSetup().getTossResult() != null) {
		   		if(session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.HOME))
		   			session_match.getSetup().setTossWinningTeam(session_match.getSetup().getHomeTeamId());
		   		else if(session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.AWAY)) 
		   			session_match.getSetup().setTossWinningTeam(session_match.getSetup().getAwayTeamId());
		   		if(session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.BAT)) 
		   			session_match.getSetup().setTossWinningDecision(CricketUtil.BAT);
		   		else if(session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.FIELD)) 
		   			session_match.getSetup().setTossWinningDecision(CricketUtil.FIELD);
			}
			
			if (session_match.getSetup().getMatchType() != null) {
				
		   		if(session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.ODI) || 
		   			session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.OD)) {
		   			session_match.getSetup().setMaxOvers(CricketUtil.ODI_MAXIMUM_OVERS);
		   		} else if(session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.IT20) 
		   			|| session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.DT20)) {
		   			session_match.getSetup().setMaxOvers(CricketUtil.T20_MAXIMUM_OVERS);
		   		} else if(session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.D10)) {
		   			session_match.getSetup().setMaxOvers(CricketUtil.D10_MAXIMUM_OVERS);
		   		} else if(session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.TEST) || 
		   			session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.FC)) {
		   			session_match.getSetup().setMaxOvers(Integer.valueOf(CricketUtil.DOT));
		   		} else if(session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.SUPER_OVER)) {
		   			session_match.getSetup().setMaxOvers(Integer.valueOf(CricketUtil.ONE));
		   		}

		   		if(session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.TEST) 
		   			|| session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.FC)) {
		   			max_inns = CricketUtil.TEST_MATCH_MAXIMUM_INNINGS;
		   		} else {
		   			max_inns = CricketUtil.LIMITED_OVER_MAXIMUM_INNINGS;
		   		}
		   		while (inns.size() < max_inns) {
		   		    inns.add(new Inning());
		   		}		   		
		   		
		   		if(!session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.TEST) && 
		   			!session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.FC)) {
		   			switch (session_match.getSetup().getMatchType().toUpperCase()) {
					case CricketUtil.ODI:
					case CricketUtil.OD:
						session_match.getSetup().setNumberOfPowerplays(3);
						for(int i=1; i<=max_inns; i++) {
							inns.get(i-1).setFirstPowerplayStartOver("0.1");
							inns.get(i-1).setFirstPowerplayEndOver("9.6");
							inns.get(i-1).setSecondPowerplayStartOver("10.1");
							inns.get(i-1).setSecondPowerplayEndOver("39.6");
							inns.get(i-1).setThirdPowerplayStartOver("40.1");
							inns.get(i-1).setThirdPowerplayEndOver("49.6");
						}
						break;
					case CricketUtil.IT20:
					case CricketUtil.DT20:
						session_match.getSetup().setNumberOfPowerplays(2);
						for(int i=1; i<=max_inns; i++) {
							inns.get(i-1).setFirstPowerplayStartOver("0.1");
							inns.get(i-1).setFirstPowerplayEndOver("5.6");
							inns.get(i-1).setSecondPowerplayStartOver("6.1");
							inns.get(i-1).setSecondPowerplayEndOver("19.6");
							inns.get(i-1).setThirdPowerplayStartOver("0.0");
							inns.get(i-1).setThirdPowerplayEndOver("0.0");
						}
						break;
					case CricketUtil.D10:
						if(session_match.getSetup().getSpecialMatchRules() != null 
							&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
							session_match.getSetup().setNumberOfPowerplays(2);
						} else {
							session_match.getSetup().setNumberOfPowerplays(1);
						}
						for(int i=1; i<=max_inns; i++) {
							inns.get(i-1).setFirstPowerplayStartOver("0.1");
							inns.get(i-1).setFirstPowerplayEndOver("1.6");
							inns.get(i-1).setSecondPowerplayStartOver("6.1");
							inns.get(i-1).setSecondPowerplayEndOver("6.6");
							inns.get(i-1).setThirdPowerplayStartOver("0.0");
							inns.get(i-1).setThirdPowerplayEndOver("0.0");
						}
						break;
					}
		   		}
	   			for(int i=1; i<=max_inns; i++) {
	   				if(inns.get(i-1).getBattingCard() == null || inns.get(i-1).getBattingCard().size() <= 0) {
		   				inns.get(i-1).setInningNumber(i);
   			   			switch (i) {
						case 1: case 3:
			   				if((session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.HOME) && 
			   						session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.BAT))
		   			   				|| (session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.AWAY) && 
		   			   				session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.FIELD))) {
				   				inns.get(i-1).setBattingTeamId(session_match.getSetup().getHomeTeamId());
				   				inns.get(i-1).setBowlingTeamId(session_match.getSetup().getAwayTeamId());
		   			   		}else if((session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.AWAY) && 
		   			   			session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.BAT))
		   			   				|| (session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.HOME) && 
		   			   					session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.FIELD))) {
				   				inns.get(i-1).setBattingTeamId(session_match.getSetup().getAwayTeamId());
				   				inns.get(i-1).setBowlingTeamId(session_match.getSetup().getHomeTeamId());
			   				} 
							break;
						case 2: case 4:
			   				if((session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.HOME) && 
			   						session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.BAT))
		   			   				|| (session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.AWAY) && 
		   			   				session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.FIELD))) {
				   				inns.get(i-1).setBattingTeamId(session_match.getSetup().getAwayTeamId());
				   				inns.get(i-1).setBowlingTeamId(session_match.getSetup().getHomeTeamId());
		   			   		}else if((session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.AWAY) && 
		   			   			session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.BAT))
		   			   				|| (session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.HOME) && 
		   			   				session_match.getSetup().getTossResult().toUpperCase().contains(CricketUtil.FIELD))) {
				   				inns.get(i-1).setBattingTeamId(session_match.getSetup().getHomeTeamId());
				   				inns.get(i-1).setBowlingTeamId(session_match.getSetup().getAwayTeamId());
			   				} 
							break;
						}
   			   			switch (i) {
						case 1: 
		   			   		inns.get(i-1).setIsCurrentInning(CricketUtil.YES);
		   			   		inns.get(i-1).setInningStatus(CricketUtil.START);
							break;
						default:
		   			   		inns.get(i-1).setIsCurrentInning(CricketUtil.NO);
		   			   		inns.get(i-1).setInningStatus(CricketUtil.PAUSE);
							break;
						}
   			   			inns.get(i-1).setBattingCard(new ArrayList<BattingCard>());
		   				if(inns.get(i-1).getBattingTeamId() == session_match.getSetup().getHomeTeamId()) {
		   					for(Player plyr : session_match.getSetup().getHomeSquad()) {
		   						inns.get(i-1).getBattingCard().add(new BattingCard(plyr.getPlayerId(), plyr.getPlayerPosition(), CricketUtil.STILL_TO_BAT));
		   					}
		   				} else if(inns.get(i-1).getBattingTeamId() == session_match.getSetup().getAwayTeamId()) {
		   					for(Player plyr : session_match.getSetup().getAwaySquad()) {
		   						inns.get(i-1).getBattingCard().add(new BattingCard(plyr.getPlayerId(), plyr.getPlayerPosition(), CricketUtil.STILL_TO_BAT));
		   					}
		   				}
		   				
	   				} else {
	   					
	   					battingCardList = objectMapper.readValue(objectMapper.writeValueAsString(inns.get(i-1).getBattingCard()),new TypeReference<List<BattingCard>>() {});
   			   			inns.get(i-1).setBattingCard(new ArrayList<BattingCard>());
		   				if(inns.get(i-1).getBattingTeamId() == session_match.getSetup().getHomeTeamId()) {
	   						for(Player plyr : session_match.getSetup().getHomeSquad()) {
	   							this_batting_card = battingCardList.stream().filter(
	   								bc -> bc.getPlayerId() == plyr.getPlayerId()).findAny().orElse(null);
			   					if(this_batting_card != null) {
			   						inns.get(i-1).getBattingCard().add(this_batting_card);
			   					} else {
			   						inns.get(i-1).getBattingCard().add(new BattingCard(plyr.getPlayerId(), 
   										plyr.getPlayerPosition(), CricketUtil.STILL_TO_BAT));
			   					}
	   						}
	   						for(Player plyr : session_match.getSetup().getHomeSubstitutes()) {
	   							this_batting_card = battingCardList.stream().filter(
	   								bc -> bc.getPlayerId() == plyr.getPlayerId()).findAny().orElse(null);
			   					if(this_batting_card != null) {
			   						inns.get(i-1).getBattingCard().add(this_batting_card);
			   					}
	   						}
	   						for(Player plyr : session_match.getSetup().getHomeOtherSquad()) {
	   							this_batting_card = battingCardList.stream().filter(
	   								bc -> bc.getPlayerId() == plyr.getPlayerId()).findAny().orElse(null);
			   					if(this_batting_card != null) {
			   						inns.get(i-1).getBattingCard().add(this_batting_card);
			   					}
	   						}
		   				} else if(inns.get(i-1).getBattingTeamId() == session_match.getSetup().getAwayTeamId()) {
	   						for(Player plyr : session_match.getSetup().getAwaySquad()) {
	   							this_batting_card = battingCardList.stream().filter(
	   								bc -> bc.getPlayerId() == plyr.getPlayerId()).findAny().orElse(null);
			   					if(this_batting_card != null) {
			   						inns.get(i-1).getBattingCard().add(this_batting_card);
			   					} else {
			   						inns.get(i-1).getBattingCard().add(new BattingCard(plyr.getPlayerId(), 
   										plyr.getPlayerPosition(), CricketUtil.STILL_TO_BAT));
			   					}
	   						}
	   						for(Player plyr : session_match.getSetup().getAwaySubstitutes()) {
	   							this_batting_card = battingCardList.stream().filter(
	   								bc -> bc.getPlayerId() == plyr.getPlayerId()).findAny().orElse(null);
			   					if(this_batting_card != null) {
			   						inns.get(i-1).getBattingCard().add(this_batting_card);
			   					}
	   						}
	   						for(Player plyr : session_match.getSetup().getAwayOtherSquad()) {
	   							this_batting_card = battingCardList.stream().filter(
	   								bc -> bc.getPlayerId() == plyr.getPlayerId()).findAny().orElse(null);
			   					if(this_batting_card != null) {
			   						inns.get(i-1).getBattingCard().add(this_batting_card);
			   					}
	   						}
		   				}	 
	   				}
	   			}
   				//Remove duplicates from batting card
	   			for (Inning inn : inns) {
	   				inn.setBattingCard(new ArrayList<>(new TreeSet<BattingCard>(
   						Comparator.comparing(BattingCard::getPlayerId)) {{
   					    addAll(new ArrayList<BattingCard>(inn.getBattingCard()));
   					}}));
	   				Collections.sort(inn.getBattingCard());
				}
		   		if((session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.TEST) 
		   			|| session_match.getSetup().getMatchType().equalsIgnoreCase(CricketUtil.FC)) 
		   			&& inns.size() >= CricketUtil.TEST_MATCH_MAXIMUM_INNINGS) {
					
		   			Inning this_inn;
					if(session_match.getSetup().getFollowOn() != null && session_match.getSetup().getFollowOn().equalsIgnoreCase(CricketUtil.YES)) {
						if(inns.get(1).getBattingTeamId() != inns.get(2).getBattingTeamId()) { // When 2nd & 3rd innings batting team are NOT the same
							this_inn = inns.get(2);
							inns.set(2, inns.get(3)); inns.get(2).setInningNumber(3);
							inns.set(3, this_inn); inns.get(3).setInningNumber(4);
						}
					} else { // Its not a follow on
						if(inns.get(1).getBattingTeamId() == inns.get(2).getBattingTeamId()) { // When 2nd & 3rd innings batting team are the same
							this_inn = inns.get(2);
							inns.set(2, inns.get(3)); inns.get(2).setInningNumber(3);
							inns.set(3, this_inn); inns.get(3).setInningNumber(4);
						}
					}
					
	   			}
   				session_match.getMatch().setInning(new ArrayList<Inning>(inns));
		   		System.out.println("session_match.getMatch().setInning = " + session_match.getMatch().getInning());
			}
			
		} else if(request.getRequestURI().contains("upload_shot_data") || request.getRequestURI().contains("upload_wagon_data")) {

			if(request.getParameterMap().entrySet().size() > 0) {

				if(session_match.getMatch().getShots() == null || session_match.getMatch().getShots().size() <= 0) {
					session_match.getMatch().setShots(new ArrayList<Shot>());
				}
				if(session_match.getMatch().getWagons() == null || session_match.getMatch().getWagons().size() <= 0) {
					session_match.getMatch().setWagons(new ArrayList<Wagon>());
				}
				if(session_match.getEventFile() == null) {
					session_match.setEventFile(new EventFile());
				}
				if(session_match.getEventFile().getEvents() == null || session_match.getEventFile().getEvents().size() <= 0) {
					session_match.getEventFile().setEvents(new ArrayList<Event>());
				}

				if(session_match.getEventFile().getEvents().size() > 0) {
				
//					int total_runs = session_match.getEventFile().getEvents().get(session_match.getEventFile().getEvents().size()-1).getEventRuns()
//						+ session_match.getEventFile().getEvents().get(session_match.getEventFile().getEvents().size()-1).getEventExtraRuns()
//						+ session_match.getEventFile().getEvents().get(session_match.getEventFile().getEvents().size()-1).getEventSubExtraRuns();

					for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
						for(Inning inn : session_match.getMatch().getInning()) {
							if(inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
								if(request.getRequestURI().contains("upload_wagon_data")) {
									if(entry.getKey().contains("wagonData")) {
										session_match.getMatch().getWagons().add(new Wagon(session_match.getMatch().getWagons().size() + 1, 
											Integer.parseInt(entry.getValue()[0].split(",")[0]),Integer.parseInt(entry.getValue()[0].split(",")[1]),
											Integer.parseInt(entry.getValue()[0].split(",")[2]), entry.getValue()[0].split(",")[3], 
											Integer.parseInt(entry.getValue()[0].split(",")[4]), 
											session_match.getEventFile().getEvents().get(session_match.getEventFile().getEvents().size()-1).getEventBatterNo(), 
											session_match.getEventFile().getEvents().get(session_match.getEventFile().getEvents().size()-1).getEventBowlerNo(),
											inn.getTotalRuns(), inn.getInningNumber(), inn.getTotalOvers(), inn.getTotalBalls()));
									}
								}
								if(request.getRequestURI().contains("upload_shot_data")) {
									if(entry.getKey().contains("shotData")) {
										session_match.getMatch().getShots().add(new Shot(session_match.getMatch().getShots().size()+1, 
											entry.getValue()[0].trim(), session_match.getEventFile().getEvents().get(
											session_match.getEventFile().getEvents().size()-1).getEventBatterNo(), 
											session_match.getEventFile().getEvents().get(session_match.getEventFile().getEvents().size()-1).getEventBowlerNo(),
											inn.getTotalRuns(), inn.getInningNumber(), inn.getTotalOvers(), inn.getTotalBalls()));
									}
								}
							}
						}
					}
				}
			}
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");
		}
		if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
			session_match = CricketFunctions.populateMatchVariables(cricketService, session_match);
		}
		
		if(request.getRequestURI().contains("upload_shot_data") || request.getRequestURI().contains("upload_wagon_data")){
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE, CricketUtil.MATCH, session_match, session_config);
		} else {
			if(reset_all_variables == true) {
				CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE, CricketUtil.SETUP + "," + CricketUtil.MATCH + "," + CricketUtil.EVENT, session_match, session_config);
			} else {
				CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE, CricketUtil.SETUP + "," + CricketUtil.MATCH, session_match, session_config);
			}
		}
		return objectMapper.writeValueAsString(session_match);
	}
	
	public void cleanMatchDataForHistoricData() throws IllegalAccessException, InvocationTargetException
	{
		for (Inning inn : session_match.getMatch().getInning()) {
			inn.setTotalRuns(0);
			inn.setTotalWickets(0);
			inn.setTotalOvers(0);
			inn.setTotalBalls(0);
			inn.setTotalExtras(0);
			inn.setTotalWides(0);
			inn.setTotalNoBalls(0);
			inn.setTotalLegByes(0);
			inn.setTotalByes(0);
			inn.setTotalPenalties(0);
			inn.setTotalFours(0);
			inn.setTotalSixes(0);
			inn.setTotalNines(0);
			inn.setRunRate("");
			inn.setBattingCard(new ArrayList<BattingCard>());
			if(inn.getBattingTeamId() == session_match.getSetup().getHomeTeamId()) {
				for(Player plyr : session_match.getSetup().getHomeSquad()) {
					inn.getBattingCard().add(new BattingCard(plyr.getPlayerId(), plyr.getPlayerPosition(), CricketUtil.STILL_TO_BAT));
				}
			} else if(inn.getBattingTeamId() == session_match.getSetup().getAwayTeamId()) {
				for(Player plyr : session_match.getSetup().getAwaySquad()) {
					inn.getBattingCard().add(new BattingCard(plyr.getPlayerId(), plyr.getPlayerPosition(), CricketUtil.STILL_TO_BAT));
				}
			}
			//Remove duplicates
			inn.setBattingCard(new ArrayList<>(new TreeSet<BattingCard>(
				Comparator.comparing(BattingCard::getPlayerId)) {{
			    addAll(inn.getBattingCard());
			}}));
			inn.setBowlingCard(new ArrayList<BowlingCard>());
			inn.setPartnerships(new ArrayList<Partnership>());
			inn.setFallsOfWickets(new ArrayList<FallOfWicket>());
			inn.setSpells(new ArrayList<Spell>());
		}
		session_match = CricketFunctions.populateMatchVariables(cricketService,session_match);
		session_match.getEventFile().setEvents(new ArrayList<Event>());
	}
	
	public String processVariousMatchDataStats(String whatToProcess,String timeStatsToProcess,String valueToProcess, Event challengeEvent) throws Exception
	{
		int batter_position = 0, firstBatter = -1, secondBatter = -1, which_bowler = 0;
		Iterator<BattingCard> bc_itr;
		BattingCard this_bc = new BattingCard();
		BowlingCard this_bwc = new BowlingCard();
		Event this_event = new Event(whatToProcess, valueToProcess);
		Inning this_inn = new Inning();
		List<DaySession> thisBatSess = new ArrayList<DaySession>();
		DaySession thisDaySession = new DaySession();
		boolean lastBallOfTheOver = false;

		switch (whatToProcess.toUpperCase()) {
		case CricketUtil.LOG_WICKET: 

			if(!valueToProcess.trim().isEmpty()) {
				
				for(Inning inn:session_match.getMatch().getInning()) {
				
					if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
						
						if(session_match.getEventFile() == null)
							session_match.setEventFile(new EventFile());
						if(session_match.getEventFile().getEvents() == null)
							session_match.getEventFile().setEvents(new ArrayList<Event>());
						
						this_event.setEventNumber(session_match.getEventFile().getEvents().size() + 1);
						this_event.setEventType(whatToProcess);
						this_event.setEventInningNumber(inn.getInningNumber());
						
						if(inn.getBowlingCard() != null && inn.getBowlingCard().size() > 0) {
							for(BowlingCard bc:inn.getBowlingCard()) {
								if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.CURRENT + CricketUtil.BOWLER)
									|| bc.getStatus().equalsIgnoreCase(CricketUtil.LAST + CricketUtil.BOWLER)) 
								{
									which_bowler = bc.getPlayerId();
									this_event.setEventBowlerNo(which_bowler);
									if(session_speed != null && session_speed.getSpeedValue() != null && Double.valueOf(session_speed.getSpeedValue()) > 0) {
										this_event.setEventSpeed(session_speed.getSpeedValue());
									}
									this_event.setEventHowOut(valueToProcess.split(",")[0].toUpperCase());
									switch (valueToProcess.split(",")[0].toUpperCase()) {
									case CricketUtil.CAUGHT_AND_BOWLED: case CricketUtil.CAUGHT: case CricketUtil.BOWLED: case CricketUtil.STUMPED: 
									case CricketUtil.LBW: case CricketUtil.HIT_WICKET:
										bc.setWickets(bc.getWickets() + 1);
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalWickets(ds.getTotalWickets() + 1);
													}
												}
											}
										}
										
										for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
											if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
												inn.getSpells().get(i).setWickets(inn.getSpells().get(i).getWickets() + 1);
											}
										}
										if(valueToProcess.split(",")[0].equalsIgnoreCase(CricketUtil.CAUGHT_AND_BOWLED))
											bc.setCatchAsBowler(bc.getCatchAsBowler() + 1);
										break;
									}
									bc.setRuns(bc.getRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
									if(session_match.getMatch().getDaysSessions() != null) {
										thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
											ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
										if(thisDaySession != null) {
											for(DaySession ds : bc.getBowlingSession()) {
												if(ds.getDayNumber() == thisDaySession.getDayNumber() 
													&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
													ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
												}
											}
										}
									}
									bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + Integer.valueOf(valueToProcess.split(",")[4]));
									this_event.setEventRuns(Integer.valueOf(valueToProcess.split(",")[4]));
									for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
										if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
											inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
										}
									}
									switch (valueToProcess.split(",")[0].toUpperCase()) {
									case CricketUtil.ABSENT_HURT: case CricketUtil.RETIRED_HURT: case CricketUtil.MANKAD: 
									case CricketUtil.RETIRED_OUT: case CricketUtil.TIMED_OUT:
										break;
									default:
										switch (valueToProcess.split(",")[4]) {
										case CricketUtil.DOT: 
											bc.setDots(bc.getDots() + 1);
										}
										if(bc.getBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver())) {
											bc.setBalls(Integer.valueOf(CricketUtil.DOT));
											bc.setOvers(bc.getOvers() + 1);
										} else {
											bc.setBalls(bc.getBalls() + 1);
										}
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalBalls(ds.getTotalBalls() + 1);
													}
												}
											}
										}
										if(inn.getTotalBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver()))
										{
											inn.setTotalBalls(Integer.valueOf(CricketUtil.DOT));
											inn.setTotalOvers(inn.getTotalOvers() + 1);
											lastBallOfTheOver = true;
										} else {
											inn.setTotalBalls(inn.getTotalBalls() + 1);
										}
										for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
											if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
												inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() + 1);
											}
										}
										if(session_match.getMatch().getDaysSessions() != null) {
											for(DaySession ds : session_match.getMatch().getDaysSessions()) {
												if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
													ds.setTotalBalls(ds.getTotalBalls() + 1);
												}
											}
										}
										break;
									}
									bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
									this_event.setEventOverNo(inn.getTotalOvers());
									this_event.setEventBallNo(inn.getTotalBalls());
									
									inn.setTotalRuns(inn.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
									if(session_match.getSetup().getSpecialMatchRules() != null 
										&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
										if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
											if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
												[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
												|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
											{
												if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
													if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
														inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
													} 
//													else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//														inn.setSpecialRuns("0");
//													} 
													else {
														inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
													}
												}
											}
										}
									}													
									if(session_match.getMatch().getDaysSessions() != null) {
										for(DaySession ds : session_match.getMatch().getDaysSessions()) {
											if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
												ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
											}
										}
									}
									inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
									if(session_match.getSetup().getSpeedFilePath() != null && !session_match.getSetup().getSpeedFilePath().isEmpty() 
										&& session_match.getMatch().getCurrent_speed() != null && !session_match.getMatch().getCurrent_speed().isEmpty()) {
										if(bc.getSpeeds() == null) {
											bc.setSpeeds(new ArrayList<Speed>());
										}
										bc.getSpeeds().add(new Speed(bc.getSpeeds().size() + 1, session_match.getMatch().getCurrent_speed(), 
											"", bc.getOvers(), bc.getBalls(), inn.getTotalOvers(), inn.getTotalBalls(), inn.getInningNumber()));
									}
									
								} else if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.OTHER + CricketUtil.BOWLER)) {
									this_event.setEventOtherBowlerNo(bc.getPlayerId());
								}
							}
						} else {
							this_event.setEventHowOut(valueToProcess.split(",")[0].toUpperCase());
							this_event.setEventRuns(Integer.valueOf(valueToProcess.split(",")[4]));
						}

						for(BattingCard bc:inn.getBattingCard()) {
							if(bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[3])) {
								this_event.setEventBatterNo(bc.getPlayerId());
								bc.setRuns(bc.getRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
								this_event.setEventOnStrike(bc.getOnStrike());
								if(session_match.getMatch().getDaysSessions() != null) {
									thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
										ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
									if(thisDaySession != null) {
										if(bc.getBattingSession() == null) {
											bc.setBattingSession(new ArrayList<DaySession>());
										}
										for(DaySession ds : bc.getBattingSession()) {
											if(ds.getDayNumber() == thisDaySession.getDayNumber() 
												&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
												ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
											}
										}
									}
								}
								if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
								} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() + Integer.valueOf(valueToProcess.split(",")[4]));
								}
								switch (valueToProcess.split(",")[0].toUpperCase()) {
								case CricketUtil.ABSENT_HURT: case CricketUtil.RETIRED_HURT: case CricketUtil.MANKAD: 
								case CricketUtil.RETIRED_OUT: case CricketUtil.TIMED_OUT:
									break;
								default:
									bc.setBalls(bc.getBalls() + 1);
									if(session_match.getMatch().getDaysSessions() != null) {
										thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
											ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
										if(thisDaySession != null) {
											for(DaySession ds : bc.getBattingSession()) {
												if(ds.getDayNumber() == thisDaySession.getDayNumber() 
													&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
													ds.setTotalBalls(ds.getTotalBalls() + 1);
												}
											}
										}
									}
									if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() + 1);
									} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() + 1);
									}
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);										
									break;
								}
								bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
							}
							if(bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1])) {
								if(valueToProcess.split(",").length >= 6 && !valueToProcess.split(",")[5].trim().isEmpty()) { // Concussion replacement player ID found
									this_event.setEventConcussionReplacePlayerId(Integer.valueOf(valueToProcess.split(",")[5]));
									bc.setConcussionPlayerId(Integer.valueOf(valueToProcess.split(",")[5]));
								}
								bc.setOnStrike("");
								this_event.setEventHowOutBatterNo(bc.getPlayerId());
								if(!valueToProcess.split(",")[0].trim().isEmpty()) { // How out text found
									switch (valueToProcess.split(",")[0].toUpperCase()) {
									case CricketUtil.RETIRED_HURT: 
										bc.setStatus(CricketUtil.STILL_TO_BAT);
										break;
									default:
										bc.setStatus(CricketUtil.OUT);
										inn.setTotalWickets(inn.getTotalWickets() + 1);
										if(session_match.getMatch().getDaysSessions() != null) {
											for(DaySession ds : session_match.getMatch().getDaysSessions()) {
												if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
													ds.setTotalWickets(ds.getTotalWickets() + 1);
												}
											}
										}
										if(inn.getFallsOfWickets() == null || inn.getFallsOfWickets().size() <= 0) {
											inn.setFallsOfWickets(new ArrayList<FallOfWicket>());
										}
										if(session_match.getSetup().getSpecialMatchRules() != null 
											&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
											inn.getFallsOfWickets().add(new FallOfWicket(inn.getFallsOfWickets().size() + 1, bc.getPlayerId(),
										        inn.getTotalRuns() + (inn.getSpecialRuns() == null ? 0 : Integer.parseInt(inn.getSpecialRuns().trim())),
										        inn.getTotalOvers(), inn.getTotalBalls(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))));
										} else {
											inn.getFallsOfWickets().add(new FallOfWicket(inn.getFallsOfWickets().size() + 1, 
												bc.getPlayerId(), inn.getTotalRuns(), inn.getTotalOvers(), inn.getTotalBalls(),
												LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))));
										}
										break;
									}
									bc.setHowOut(valueToProcess.split(",")[0]);
									switch (valueToProcess.split(",")[0].toUpperCase()) {
									case CricketUtil.CAUGHT_AND_BOWLED: case CricketUtil.CAUGHT: case CricketUtil.BOWLED: 
									case CricketUtil.STUMPED: case CricketUtil.LBW: case CricketUtil.HIT_WICKET: case CricketUtil.MANKAD:
										bc.setHowOutBowlerId(which_bowler);
										break;
									}
									switch (valueToProcess.split(",")[0].toUpperCase()) {
									case CricketUtil.CAUGHT: case CricketUtil.RUN_OUT: case CricketUtil.MANKAD: case CricketUtil.STUMPED:
										bc.setHowOutFielderId(Integer.valueOf(valueToProcess.split(",")[2]));
										this_event.setEventHowOutFielderId(Integer.valueOf(valueToProcess.split(",")[2]));
										bc.setWasHowOutFielderSubstitute(valueToProcess.split(",")[6]);
										this_event.setSubstitutionMade(valueToProcess.split(",")[6]);
										break;
									}
									bc.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
									bc=CricketFunctions.processBattingcard(cricketService,bc);
								}
							}
						}
						
						for(BattingCard bc:inn.getBattingCard()) {
							if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
								if(lastBallOfTheOver == true) {
									switch (valueToProcess.split(",")[4]) {
									case CricketUtil.DOT: case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: case CricketUtil.NINE: 
										bc.setOnStrike(CricketUtil.NO);
										break;
									}
								} else {
									switch (valueToProcess.split(",")[4]) {
									case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
										bc.setOnStrike(CricketUtil.NO);
										break;
									}
								}
							} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.NO)) 
							{
								this_event.setEventOtherBatterNo(bc.getPlayerId());
								if(lastBallOfTheOver == true) {
									switch (valueToProcess.split(",")[4]) {
									case CricketUtil.DOT: case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: case CricketUtil.NINE:
										bc.setOnStrike(CricketUtil.YES);
										break;
									}
								} else {
									switch (valueToProcess.split(",")[4]) {
									case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
										bc.setOnStrike(CricketUtil.YES);
										break;
									}
								}							
							}
						}
						
						switch (valueToProcess.split(",")[0].toUpperCase()) {
						case CricketUtil.CONCUSSED:
							inn.getBattingCard().add(CricketFunctions.processBattingcard(cricketService, 
								new BattingCard(Integer.valueOf(valueToProcess.split(",")[5]),
								inn.getBattingCard().size() + 1, CricketUtil.STILL_TO_BAT)));
							this_event.setEventConcussionReplacePlayerId(Integer.valueOf(valueToProcess.split(",")[5]));
							break;
						}
					}
				}
			}

			for(Inning inn : session_match.getMatch().getInning()) {
				if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
					if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
						session_match.getMatch().setMatchStatus(CricketFunctions.GenerateMatchSummaryStatus(
							inn.getInningNumber(), session_match, "", "", "", false).getTargetOrResult());
					}
				}
			}
			
			session_match.getEventFile().setStatus(CricketFunctions.findConsecutiveDupicateEvents(session_match.getEventFile().getEvents(), this_event));
			session_match.getEventFile().getEvents().add(this_event);
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");

			return objectMapper.writeValueAsString(session_match);

		case CricketUtil.CHANGE_BOWLER:
			
			if(!valueToProcess.trim().isEmpty()) {
				
				if(session_match.getEventFile() == null)
					session_match.setEventFile(new EventFile());
				if(session_match.getEventFile().getEvents() == null)
					session_match.getEventFile().setEvents(new ArrayList<Event>());
				
				this_event.setEventNumber(session_match.getEventFile().getEvents().size() + 1);
				this_event.setEventType(whatToProcess);
				
				for(Inning inn:session_match.getMatch().getInning()) {
				
					if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {

						this_event.setEventInningNumber(inn.getInningNumber());
						this_event.setEventRuns(inn.getTotalRuns());
						this_event.setEventOverNo(inn.getTotalOvers());
						this_event.setEventBallNo(inn.getTotalBalls());

						if(inn.getBowlingCard() != null) {

							for(BowlingCard bc:inn.getBowlingCard()) {
								if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.CURRENT + CricketUtil.BOWLER)
										|| bc.getStatus().equalsIgnoreCase(CricketUtil.LAST + CricketUtil.BOWLER)) {
									bc.setStatus(CricketUtil.OTHER + CricketUtil.BOWLER);
									this_event.setEventOtherBowlerNo(bc.getPlayerId());
								}
							}
							
							for(BowlingCard bc:inn.getBowlingCard()) {

								if(bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[0])) {
									bc.setStatus(CricketUtil.CURRENT + CricketUtil.BOWLER);
									if(session_match.getMatch().getDaysSessions() != null) {
										thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
											ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
										if(thisDaySession != null) {
											if(bc.getBowlingSession() == null) {
												bc.setBowlingSession(new ArrayList<DaySession>());
											}
											which_bowler = 0;
											for (DaySession bowlSess : bc.getBowlingSession()) {
												if(thisDaySession.getDayNumber() == bowlSess.getDayNumber() 
													&& thisDaySession.getSessionNumber() == bowlSess.getSessionNumber()) {
													which_bowler = bc.getPlayerId();
												}
											}
											if(which_bowler <= 0) {
												bc.getBowlingSession().add(new DaySession(thisDaySession.getDayNumber(), thisDaySession.getSessionNumber()));
											}
										}
									}
									bc.setTotalRunsThisOver(0);
									bc.setBowling_end(Integer.valueOf(valueToProcess.split(",")[1]));
									if(session_match.getSetup().getSpecialMatchRules() != null 
										&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)
										&& valueToProcess.split(",").length >= 3) {
										if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
											bc.setBallTypeOverNo(bc.getBallTypeOverNo() + "," + valueToProcess.split(",")[3]);
										} else {
											bc.setBallTypeOverNo(valueToProcess.split(",")[3]);
										}
										this_event.setEventExtra(valueToProcess.split(",")[3]);
										this_event.setEventSubExtra(valueToProcess.split(",")[4]);
									}
									which_bowler = bc.getPlayerId();
									this_event.setEventBowlerNo(which_bowler);
									this_event.setEventBowlingEnd(Integer.valueOf(valueToProcess.split(",")[1]));
								}
							}

						} else {
							
							inn.setBowlingCard(new ArrayList<BowlingCard>());

						}
						if(inn.getSpells() == null) {
							inn.setSpells(new ArrayList<Spell>());
						}
						if(inn.getSpells().stream().filter(sp -> sp.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[0]) && 
								sp.getSpellNumber() == Integer.valueOf(valueToProcess.split(",")[2])).findAny().orElse(null) == null) {
							inn.getSpells().add(new Spell(Integer.valueOf(valueToProcess.split(",")[2]), Integer.valueOf(valueToProcess.split(",")[0])));
						}
						if(which_bowler <= 0) {
							
							inn.getBowlingCard().add(new BowlingCard(cricketService.getPlayer(CricketUtil.PLAYER, 
								valueToProcess.split(",")[0]), inn.getBowlingCard().size() + 1, 
								CricketUtil.CURRENT + CricketUtil.BOWLER, Integer.valueOf(valueToProcess.split(",")[1])));
							if(session_match.getMatch().getDaysSessions() != null) {
								thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
									ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
								if(thisDaySession != null) {
									thisBatSess.add(new DaySession(thisDaySession.getDayNumber(), thisDaySession.getSessionNumber()));
									inn.getBowlingCard().get(inn.getBowlingCard().size()-1).setBowlingSession(thisBatSess);
								}
							}
							if(session_match.getSetup().getSpecialMatchRules() != null 
								&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)
								&& valueToProcess.split(",").length >= 3) {
								if(inn.getBowlingCard().get(inn.getBowlingCard().size()-1).getBallTypeOverNo() != null 
									&& !inn.getBowlingCard().get(inn.getBowlingCard().size()-1).getBallTypeOverNo().isEmpty()) {
									inn.getBowlingCard().get(inn.getBowlingCard().size()-1).setBallTypeOverNo(
										inn.getBowlingCard().get(inn.getBowlingCard().size()-1).getBallTypeOverNo() + "," + valueToProcess.split(",")[3]);
								} else {
									inn.getBowlingCard().get(inn.getBowlingCard().size()-1).setBallTypeOverNo(valueToProcess.split(",")[3]);
								}
								this_event.setEventExtra(valueToProcess.split(",")[3]);
								this_event.setEventSubExtra(valueToProcess.split(",")[4]);
							}
							this_event.setEventBowlerNo(Integer.valueOf(valueToProcess.split(",")[0]));
							this_event.setEventBowlingEnd(Integer.valueOf(valueToProcess.split(",")[1]));
						}
					}
				}
			}

			session_match.getEventFile().setStatus(CricketFunctions.findConsecutiveDupicateEvents(session_match.getEventFile().getEvents(), this_event));
			session_match.getEventFile().getEvents().add(this_event);
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);

			return objectMapper.writeValueAsString(session_match);
		
		case "LOG_UNDO_IMPACT":
			
			if(session_match.getEventFile().getEvents() != null &&
				session_match.getEventFile().getEvents().size() > 0) {

				this_event = session_match.getEventFile().getEvents().stream().filter(
					evnt -> evnt.getEventNumber() == Integer.valueOf(valueToProcess) 
					&& evnt.getEventType().equalsIgnoreCase("LOG_IMPACT")).findAny().orElse(null);
				
				if(this_event != null) {
					
					for (Player plyr : session_match.getSetup().getHomeSquad()) {
						if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
							plyr.setSubstitutionType("");
						}
					}
					for (Player plyr : session_match.getSetup().getHomeSubstitutes()) {
						if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
							plyr.setSubstitutionType("");
						}
					}
					for (Player plyr : session_match.getSetup().getHomeOtherSquad()) {
						if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
							plyr.setSubstitutionType("");
						}
					}
					for (Player plyr : session_match.getSetup().getAwaySquad()) {
						if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
							plyr.setSubstitutionType("");
						}
					}
					for (Player plyr : session_match.getSetup().getAwaySubstitutes()) {
						if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
							plyr.setSubstitutionType("");
						}
					}
					for (Player plyr : session_match.getSetup().getAwayOtherSquad()) {
						if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
							plyr.setSubstitutionType("");
						}
					}	
					if(session_match.getSetup().getSpecialMatchRules() != null 
						&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
					}
					else {
						for(Inning inn:session_match.getMatch().getInning()) {
							if(inn.getInningNumber() == this_event.getEventInningNumber()) {
								bc_itr = inn.getBattingCard().iterator();
								while(bc_itr.hasNext()) {
									this_bc = bc_itr.next();
									if(this_bc.getPlayerId() == this_event.getEventBatterNo()) {
										bc_itr.remove();
										break;
									}
								}	
								if(this_event.getEventBattingCard() != null) {
									if(this_event.getEventBattingCard().getBatsmanInningStarted() != null 
										&& this_event.getEventBattingCard().getBatsmanInningStarted().equalsIgnoreCase(CricketUtil.YES)) {
									} else {
										inn.getBattingCard().add(this_event.getEventBattingCard());
									}
								}
								Collections.sort(inn.getBattingCard());
							}
						}		
					}
					session_match.getEventFile().getEvents().remove(this_event);
					
					session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
					//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
					//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
					CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);
					//CricketFunctions.getInteractive(session_match, "FULL_WRITE");
				}
			}
			return objectMapper.writeValueAsString(session_match);
		
		case "JUMP_TO_HISTORIC_POINT":

			EventFile backUpSession_eventFile = objectMapper.readValue(
				objectMapper.writeValueAsString(session_match.getEventFile()), EventFile.class);
			cleanMatchDataForHistoricData();
			backMatchData = false;
			
			if(backUpSession_eventFile != null && backUpSession_eventFile.getEvents() != null 
				&& backUpSession_eventFile.getEvents().size() > 0) {
				for (Event evnt : backUpSession_eventFile.getEvents()) {
					if(evnt.getEventType().equalsIgnoreCase(CricketUtil.END_OVER)
						&& evnt.getEventInningNumber() >= Integer.valueOf(valueToProcess.split(",")[0])
						&& evnt.getEventOverNo() >= Integer.valueOf(valueToProcess.split(",")[1])) {
						break;
					} else {
//						processCricketProcedures(evnt.getWhatToProcess(), "", evnt.getValueToProcess());
					}
				}
			}
			session_match.getSetup().setHistoricMatchLoaded(true);			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");
			return objectMapper.writeValueAsString(session_match);

		case CricketUtil.LOAD_MATCH: case CricketUtil.LOAD_SETUP: case "LOAD_MATCH_AFTER_WAGON_LOG": case "LOAD_BACKUP_MATCH":

			switch (whatToProcess.toUpperCase()) {
			case "LOAD_BACKUP_MATCH": 
				Files.copy(Paths.get(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY 
					+ CricketUtil.MATCHES_DIRECTORY + session_match.getMatch().getMatchFileName()), 
					Paths.get(masterCricketDirectory + CricketUtil.MATCHES_DIRECTORY 
					+ session_match.getMatch().getMatchFileName()), StandardCopyOption.REPLACE_EXISTING);
				Files.copy(Paths.get(masterCricketDirectory + CricketUtil.BACK_UP_DIRECTORY 
					+ CricketUtil.EVENT_DIRECTORY + session_match.getMatch().getMatchFileName()), 
					Paths.get(masterCricketDirectory + CricketUtil.EVENT_DIRECTORY 
					+ session_match.getMatch().getMatchFileName()), StandardCopyOption.REPLACE_EXISTING);
				backMatchData = true;
				Thread.sleep(5000);
				break;
			}
			
			switch (whatToProcess.toUpperCase()) {
			case CricketUtil.LOAD_MATCH: case "LOAD_BACKUP_MATCH": case CricketUtil.LOAD_SETUP:
				
				if(session_match.getMatch() == null) {
					session_match.setMatch(new Match());
				}
				session_match.getMatch().setMatchFileName(valueToProcess.substring(0, valueToProcess.indexOf('.')) + ".json");
				session_match = CricketFunctions.readOrSaveMatchFile(CricketUtil.READ,CricketUtil.MATCH 
					+ "," + CricketUtil.SETUP + "," + CricketUtil.EVENT,session_match, session_config);
				
				//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
//				switch (whatToProcess.toUpperCase()) {
//				case CricketUtil.LOAD_MATCH: case "LOAD_BACKUP_MATCH":
//					CricketFunctions.getInteractive(session_match, "FULL_WRITE");
//					break;
//				}
				break;
			}
			switch (whatToProcess.toUpperCase()) {
			case CricketUtil.LOAD_SETUP:
			
				if(session_match.getSetup() == null) {
					session_match.setSetup(new Setup());
				}
				session_match.getSetup().setHomeOtherSquad(CricketFunctions.getPlayersFromDB(
					cricketService, CricketUtil.HOME, session_match));
				session_match.getSetup().setAwayOtherSquad(CricketFunctions.getPlayersFromDB(
					cricketService, CricketUtil.AWAY, session_match));
				
				break;
			}
			
			session_match.getSetup().setHistoricMatchLoaded(false);

			if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
				session_match = CricketFunctions.populateMatchVariables(cricketService,session_match);
			}
			for(Inning inn : session_match.getMatch().getInning()) {
				if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
					if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
						session_match.getMatch().setMatchStatus(CricketFunctions.GenerateMatchSummaryStatus(
							inn.getInningNumber(), session_match, "", "", "", false).getTargetOrResult());
					}
				}
			}

			return objectMapper.writeValueAsString(session_match);
			
		case CricketUtil.SELECT_INNING:
			
			for(Inning inn:session_match.getMatch().getInning()) {
				if(inn.getInningNumber() == Integer.valueOf(valueToProcess)) 
					inn.setIsCurrentInning(CricketUtil.YES); 
				else
					inn.setIsCurrentInning(CricketUtil.NO);
			}

			for(Inning inn : session_match.getMatch().getInning()) {
				if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
					if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
						session_match.getMatch().setMatchStatus(CricketFunctions.GenerateMatchSummaryStatus(
							inn.getInningNumber(), session_match, "", "", "", false).getTargetOrResult());
					}
				}
			}
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE, CricketUtil.MATCH, session_match, session_config);

			return objectMapper.writeValueAsString(session_match);
			
		case CricketUtil.INNING_STATUS:

			if(valueToProcess.contains(",") == true) {

				for(Inning inn:session_match.getMatch().getInning()) {
					if(Integer.valueOf(valueToProcess.split(",")[0]) == inn.getInningNumber()) {
						inn.setIsCurrentInning(CricketUtil.YES); 
						inn.setInningStatus(valueToProcess.split(",")[1]);
						if(valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.START)) {
							inn.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
						} else {
							inn.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
						}
						Collections.sort(inn.getBattingCard());
					} else {
						inn.setIsCurrentInning(CricketUtil.NO); 
					}
				}

				for(Inning inn : session_match.getMatch().getInning()) {
					if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
						if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
							session_match.getMatch().setMatchStatus(CricketFunctions.GenerateMatchSummaryStatus(
								inn.getInningNumber(), session_match, "", "", "", false).getTargetOrResult());
						}
						if(valueToProcess.split(",").length >= 4) {
							inn.getSpells().add(new Spell(Integer.valueOf(valueToProcess.split(",")[2]), 
								Integer.valueOf(valueToProcess.split(",")[3])));
						}
					}
				}
				
				session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS", session_match.getMatch(), timeStatsToProcess,this_event));
				//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
				//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
				CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH,session_match, session_config);
			}
			
			return objectMapper.writeValueAsString(session_match);
		
		case "LOG_MATCH_DATA_UPDATE":
			
			session_match.getSetup().setMatchDataUpdate(valueToProcess);
			//Update match and events as soon as operator STARTS match update
			if(valueToProcess != null && valueToProcess.equalsIgnoreCase(CricketUtil.START)) {
				session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS", session_match.getMatch(), timeStatsToProcess, this_event));
				//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
				//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
				CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH + "," + CricketUtil.EVENT 
					+ "," + CricketUtil.SETUP,session_match, session_config);
			}
			return objectMapper.writeValueAsString(session_match);
		
		case CricketUtil.LOAD_UNDO:
			
			return objectMapper.writeValueAsString(session_match);

		case CricketUtil.LOAD_TEAMS:
			
			if(!valueToProcess.trim().isEmpty()) {
				
				if(session_match.getSetup() == null) {
					session_match.setSetup(new Setup());
				}
				
				session_match.getSetup().setHomeTeam(cricketService.getTeam(CricketUtil.TEAM, valueToProcess.split(",")[0]));
				session_match.getSetup().setAwayTeam(cricketService.getTeam(CricketUtil.TEAM, valueToProcess.split(",")[1]));
				
				boolean correctTeamLoaded = false;
				if(session_match.getSetup().getHomeSquad() != null && session_match.getSetup().getHomeSquad().size() > 0) {
					for (Player plyr : session_match.getSetup().getHomeSquad()) {
						if(plyr.getTeamId() == Integer.valueOf(valueToProcess.split(",")[0])) {
							correctTeamLoaded = true;
						} else {
							correctTeamLoaded = false;
							break;
						}
					}
				}
				
				List<Player> loadedSquad;
				if (correctTeamLoaded == false) {
					if(valueToProcess.split(",").length >= 3) {
						loadedSquad = cricketService.getPlayers(CricketUtil.GENDER_SPECIFIC_TEAM,
					        valueToProcess.split(",")[0] + "," + valueToProcess.split(",")[2]);
					}else {
						loadedSquad = cricketService.getPlayers(CricketUtil.TEAM, valueToProcess.split(",")[0]);
					}
					session_match.getSetup().setHomeSquad(CricketFunctions.mergePlayingEleven(session_match.getSetup().getHomeSquad(), loadedSquad));
				}

				correctTeamLoaded = false;
				if(session_match.getSetup().getAwaySquad() != null && session_match.getSetup().getAwaySquad().size() > 0) {
					for (Player plyr : session_match.getSetup().getAwaySquad()) {
						if(plyr.getTeamId() == Integer.valueOf(valueToProcess.split(",")[1])) {
							correctTeamLoaded = true;
						} else {
							correctTeamLoaded = false;
							break;
						}
					}
				}
				if (correctTeamLoaded == false) {
					if(valueToProcess.split(",").length >= 3) {
						loadedSquad = cricketService.getPlayers(CricketUtil.GENDER_SPECIFIC_TEAM,
					        valueToProcess.split(",")[1] + "," + valueToProcess.split(",")[2]);
					}else {
						loadedSquad = cricketService.getPlayers(CricketUtil.TEAM, valueToProcess.split(",")[1]);
					}
					session_match.getSetup().setAwaySquad(CricketFunctions.mergePlayingEleven(session_match.getSetup().getAwaySquad(), loadedSquad));					
				}
			}
			
			return objectMapper.writeValueAsString(session_match);
		
		case "LOG_IMPACT":

			if(session_match.getEventFile() == null)
				session_match.setEventFile(new EventFile());
			
			if(session_match.getEventFile().getEvents() == null)
				session_match.getEventFile().setEvents(new ArrayList<Event>());
			
			this_event.setEventNumber(session_match.getEventFile().getEvents().size() + 1);
			this_event.setEventType(whatToProcess);
			this_event.setEventBatterNo(Integer.valueOf(valueToProcess.split(",")[2]));
			this_event.setEventOtherBatterNo(Integer.valueOf(valueToProcess.split(",")[1]));
			this_event.setEventExtra(valueToProcess.split(",")[3]);
			
			if(session_match.getSetup().getHomeSquad() != null && session_match.getSetup().getHomeSquad().size() > 0) {
				for (Player plyr : session_match.getSetup().getHomeSquad()) {
					if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[2])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.INCOMING);
					} else if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.OUTGOING);
					}
				}
			}
			if(session_match.getSetup().getHomeSubstitutes() != null && session_match.getSetup().getHomeSubstitutes().size() > 0) {
				for (Player plyr : session_match.getSetup().getHomeSubstitutes()) {
					if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[2])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.INCOMING);
					} else if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.OUTGOING);
					}
				}
			}
			if(session_match.getSetup().getHomeOtherSquad() != null && session_match.getSetup().getHomeOtherSquad().size() > 0) {
				for (Player plyr : session_match.getSetup().getHomeOtherSquad()) {
					if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[2])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.INCOMING);
					} else if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.OUTGOING);
					}
				}
			}
			if(session_match.getSetup().getAwaySquad() != null && session_match.getSetup().getAwaySquad().size() > 0) {
				for (Player plyr : session_match.getSetup().getAwaySquad()) {
					if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[2])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.INCOMING);
					} else if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.OUTGOING);
					}
				}
			}
			if(session_match.getSetup().getAwaySubstitutes() != null && session_match.getSetup().getAwaySubstitutes().size() > 0) {
				for (Player plyr : session_match.getSetup().getAwaySubstitutes()) {
					if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[2])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.INCOMING);
					} else if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.OUTGOING);
					}
				}
			}
			if(session_match.getSetup().getAwayOtherSquad() != null && session_match.getSetup().getAwayOtherSquad().size() > 0) {
				for (Player plyr : session_match.getSetup().getAwayOtherSquad()) {
					if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[2])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.INCOMING);
					} else if(plyr.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1])) {
						plyr.setSubstitutionType(CricketUtil.IMPACT + CricketUtil.OUTGOING);
					}
				}
			}
			if(session_match.getSetup().getSpecialMatchRules() != null 
				&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
			}
			else 
			{
				for(Inning inn:session_match.getMatch().getInning()) {
					if(inn.getBattingTeamId() == Integer.valueOf(valueToProcess.split(",")[0])) {
						this_event.setEventInningNumber(inn.getInningNumber());
						bc_itr = inn.getBattingCard().iterator();
						while(bc_itr.hasNext()) {
							this_bc = bc_itr.next();
							if(this_bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1])) {
								this_event.setEventBattingCard(objectMapper.readValue(objectMapper.writeValueAsString(this_bc), BattingCard.class));
								if(this_bc.getBatsmanInningStarted() != null && this_bc.getBatsmanInningStarted().equalsIgnoreCase(CricketUtil.YES)) {
									batter_position = inn.getBattingCard().size() + 1; 
								} else {
									bc_itr.remove();
									batter_position = this_bc.getBatterPosition();
								}
								break;
							}
						}	
						inn.getBattingCard().add(CricketFunctions.processBattingcard(
							cricketService, new BattingCard(Integer.valueOf(valueToProcess.split(",")[2]), batter_position, CricketUtil.STILL_TO_BAT)));
						Collections.sort(inn.getBattingCard());
					}
				}
			}
			
			if(valueToProcess.split(",").length >= 5) {
				this_event.setEventInningNumber(Integer.valueOf(valueToProcess.split(",")[4]));
			}
			this_event.setEventDescription(this_event.getEventType());
			session_match.getEventFile().setStatus(CricketFunctions.findConsecutiveDupicateEvents(
				session_match.getEventFile().getEvents(), this_event));
			session_match.getEventFile().getEvents().add(this_event);
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.SETUP + "," + CricketUtil.EVENT,session_match, session_config);
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");

			return objectMapper.writeValueAsString(session_match);
		
		case "LOG_NEW_BALL":

			for (Inning inn : session_match.getMatch().getInning()) {
				if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
					inn.setNewBallOver(String.valueOf(inn.getTotalOvers()) + "." +  String.valueOf(inn.getTotalBalls()));
				}
			}

			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH,session_match, session_config);
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");
			
			return objectMapper.writeValueAsString(session_match);
			
		case "LOG_FINISH":

			session_match.getMatch().setMatchFinishTime(new MatchFinishTime(valueToProcess.split(",")[0].replace("_", ":"), 
				valueToProcess.split(",")[1].replace("_", ":"),valueToProcess.split(",")[2].replace("_", ":"), 
				valueToProcess.split(",")[3].replace("_", ":"), valueToProcess.split(",")[4].replace("_", ":"), 
				valueToProcess.split(",")[5].replace("_", ":"), valueToProcess.split(",")[6], valueToProcess.split(",")[7]));
			
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH,session_match, session_config);
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");

			return objectMapper.writeValueAsString(session_match);
			
		case "LOG_PP_DATA":

			for(Inning inn:session_match.getMatch().getInning()) {
				if(inn.getInningNumber() == Integer.valueOf(valueToProcess.split(",")[0])) {
					switch (Integer.valueOf(valueToProcess.split(",")[1])) {
					case 1:
						inn.setFirstPowerplayStartOver(valueToProcess.split(",")[2]);
						inn.setFirstPowerplayEndOver(valueToProcess.split(",")[3]);
						break;
					case 2:
						inn.setSecondPowerplayStartOver(valueToProcess.split(",")[2]);
						inn.setSecondPowerplayEndOver(valueToProcess.split(",")[3]);
						break;
					case 3:
						inn.setThirdPowerplayStartOver(valueToProcess.split(",")[2]);
						inn.setThirdPowerplayEndOver(valueToProcess.split(",")[3]);
						break;
					}
				}
			}
			session_match.getSetup().setNumberOfPowerplays(Integer.valueOf(valueToProcess.split(",")[4]));
			
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE, CricketUtil.SETUP + "," + CricketUtil.MATCH, session_match, session_config);
			
			return objectMapper.writeValueAsString(session_match);
			
		case CricketUtil.LOG_VARIOUS:

			if (valueToProcess.toUpperCase().contains(CricketUtil.BOWLER_RUNNING)
				|| valueToProcess.toUpperCase().contains(CricketUtil.BALL_RELEASE)) 
			{

				if (valueToProcess.toUpperCase().contains(CricketUtil.BOWLER_RUNNING)) {
					session_match.getMatch().setBowlerRunning(CricketUtil.YES);				
				}else if (valueToProcess.toUpperCase().contains(CricketUtil.BALL_RELEASE)) {
					session_match.getMatch().setBallRelease(CricketUtil.YES);				
				}
				session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
				//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
				//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
				CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE, CricketUtil.MATCH,session_match, session_config);
				
			} else if(valueToProcess.toUpperCase().contains("RELOAD_MATCH")) 
			{
				
				if(session_match.getMatch().getMatchFileName() != null 
						&& !session_match.getMatch().getMatchFileName().isEmpty()) {
					session_match = CricketFunctions.readOrSaveMatchFile(CricketUtil.READ, 
						CricketUtil.MATCH + "," + CricketUtil.SETUP + "," + CricketUtil.EVENT, session_match, session_config);
					if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
						session_match = CricketFunctions.populateMatchVariables(cricketService, session_match);
					}
				}
			} 
			else if(valueToProcess.toUpperCase().contains("UPDATE_BOWLERS_SPEEDS")) 
			{
				if(new File(masterCricketDirectory + CricketUtil.SPEED_DIRECTORY 
					+ session_match.getMatch().getMatchFileName().toLowerCase().replace(".json", ".txt")).exists()) {
					
					for (Inning inn : session_match.getMatch().getInning()) {
						if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
							inn.setBowlingCard(CricketFunctions.MergeMissingSpeeds(inn.getInningNumber(), inn.getBowlingCard(), CricketFunctions.ReadBallSpeedData(
								masterCricketDirectory + CricketUtil.SPEED_DIRECTORY + session_match.getMatch().getMatchFileName().toLowerCase().replace(".json", ".txt"))));
						}
					}
					session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
					//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
					CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE, CricketUtil.MATCH,session_match, session_config);
				}
			}
			
			return objectMapper.writeValueAsString(session_match);

		case "LOG_IS_DECLARED":

			for (Inning inn : session_match.getMatch().getInning()) {
				if(inn.getInningNumber() == Integer.valueOf(valueToProcess.split(",")[0])) {
					inn.setIsDeclared(valueToProcess.split(",")[1]);
				}
			}
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE, CricketUtil.MATCH, session_match, session_config);

			return objectMapper.writeValueAsString(session_match);
			
		case CricketUtil.LOG_DAY_SESSION:

			final int dayNum = Integer.valueOf(valueToProcess.split(",")[0]), sessNum = Integer.valueOf(valueToProcess.split(",")[1]);
			
			if(dayNum > 0 && dayNum > 0) {
				
				boolean day_and_sess_found = false;
				
				if(session_match.getMatch().getDaysSessions() == null) {
					session_match.getMatch().setDaysSessions(new ArrayList<DaySession>());
				}
				for (DaySession daySess : session_match.getMatch().getDaysSessions()) {
					if(daySess.getDayNumber() == dayNum && daySess.getSessionNumber() == sessNum) {
						daySess.setIsCurrentSession(CricketUtil.YES);
						day_and_sess_found = true;
					} else {
						daySess.setIsCurrentSession(CricketUtil.NO);
					}
				}
				if(day_and_sess_found == false) {
					session_match.getMatch().getDaysSessions().add(new DaySession(dayNum, sessNum, CricketUtil.YES));
					for (Inning inn : session_match.getMatch().getInning()) {
						if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
							for(BattingCard bc:inn.getBattingCard()) {
								if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)) {
									if(bc.getBattingSession() == null) {
										bc.setBattingSession(new ArrayList<DaySession>());
									}
									thisBatSess = bc.getBattingSession();
									if(thisBatSess.stream().filter(ds -> ds.getDayNumber() == dayNum 
										&& ds.getSessionNumber() == sessNum).findAny().orElse(null) == null)
									{
										thisBatSess.add(new DaySession(dayNum, sessNum));
										bc.setBattingSession(thisBatSess);
									}
								}
							}
							if(inn.getBowlingCard() != null && inn.getBowlingCard().size() > 0) {
								for(BowlingCard bc:inn.getBowlingCard()) {
									if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(
										CricketUtil.CURRENT + CricketUtil.BOWLER)) {
										if(bc.getBowlingSession() == null) {
											bc.setBowlingSession(new ArrayList<DaySession>());
										}
										thisBatSess = bc.getBowlingSession();
										if(thisBatSess.stream().filter(ds -> ds.getDayNumber() == dayNum 
											&& ds.getSessionNumber() == sessNum).findAny().orElse(null) == null)
										{
											thisBatSess.add(new DaySession(dayNum, sessNum));
											bc.setBowlingSession(thisBatSess);
										}
									}
								}
							}
						}
					}
				}
				session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
				//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
				//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
				CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH,session_match, session_config);
			}

			return objectMapper.writeValueAsString(session_match);
			
		case "LOG_RESULT":

			if(session_match.getEventFile() == null)
				session_match.setEventFile(new EventFile());
			
			if(session_match.getEventFile().getEvents() == null)
				session_match.getEventFile().setEvents(new ArrayList<Event>());
			
			this_event.setEventNumber(session_match.getEventFile().getEvents().size() + 1);
			this_event.setEventType(CricketUtil.RESULT);
			this_event.setEventDescription(valueToProcess);
			session_match.getEventFile().setStatus(CricketFunctions.findConsecutiveDupicateEvents(
				session_match.getEventFile().getEvents(), this_event));
			session_match.getEventFile().getEvents().add(this_event);

			session_match.getMatch().setMatchResult(valueToProcess);
			
			for(Inning inn : session_match.getMatch().getInning()) {
				if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
					if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
						session_match.getMatch().setMatchStatus(CricketFunctions.GenerateMatchSummaryStatus(
								inn.getInningNumber(), session_match, "", "", "", false).getTargetOrResult());
					}
				}
			}
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);

			return objectMapper.writeValueAsString(session_match);

		case "LOG_OVERWRITE_REVIEW":
		case CricketUtil.LOG_REVIEW:

			List<Review> this_reviews = new ArrayList<Review>();
			for(Inning inn:session_match.getMatch().getInning()) {
				if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
					switch (whatToProcess.toUpperCase()) {
					case "LOG_OVERWRITE_REVIEW":
						
						if(valueToProcess.split(",").length >= 4) {

							//Clear everything first
							inn.setReview(new ArrayList<Review>());
							session_match.getEventFile().getEvents().removeIf(evnt -> 
								evnt.getEventType().equalsIgnoreCase(CricketUtil.LOG_REVIEW) 
								&& evnt.getEventInningNumber() == inn.getInningNumber());

							//Home success review
							for(int iSucc = 1; iSucc <= Integer.valueOf(valueToProcess.split(",")[0]); iSucc++) 
							{ 
								inn.getReviews().add(new Review(inn.getReviews().size() + 1, session_match.getSetup().getHomeTeamId(), 
									CricketUtil.SUCCESSFUL, CricketUtil.RETAINED));
							}
							//Away success review
							for(int iSucc = 1; iSucc <= Integer.valueOf(valueToProcess.split(",")[1]); iSucc++) 
							{ 
								inn.getReviews().add(new Review(inn.getReviews().size() + 1, session_match.getSetup().getAwayTeamId(), 
									CricketUtil.SUCCESSFUL, CricketUtil.RETAINED));
							}
							//Home Unsuccessful review
							for(int iSucc = 1; iSucc <= Integer.valueOf(valueToProcess.split(",")[2]); iSucc++) 
							{ 
								inn.getReviews().add(new Review(inn.getReviews().size() + 1, session_match.getSetup().getHomeTeamId(), 
									CricketUtil.UNSUCCESSFUL, CricketUtil.UNRETAINED));
							}
							//Away Unsuccessful review
							for(int iSucc = 1; iSucc <= Integer.valueOf(valueToProcess.split(",")[3]); iSucc++) 
							{ 
								inn.getReviews().add(new Review(inn.getReviews().size() + 1, session_match.getSetup().getAwayTeamId(), 
									CricketUtil.UNSUCCESSFUL, CricketUtil.UNRETAINED));
							}
						}
						break;
						
					case CricketUtil.LOG_REVIEW:
						
						if(inn.getReviews() != null) {
							this_reviews = inn.getReviews();
						}
						this_reviews.add(new Review(this_reviews.size() + 1, Integer.valueOf(valueToProcess.split(",")[0]), 
							valueToProcess.split(",")[1], valueToProcess.split(",")[2]));
						inn.setReview(this_reviews);
						if(session_match.getEventFile() == null)
							session_match.setEventFile(new EventFile());
						if(session_match.getEventFile().getEvents() == null)
							session_match.getEventFile().setEvents(new ArrayList<Event>());
						this_event.setEventNumber(session_match.getEventFile().getEvents().size() + 1);
						this_event.setEventType(whatToProcess);
						this_event.setEventInningNumber(inn.getInningNumber());
						this_event.setEventStatNumber(this_reviews.size()); // review ID
						session_match.getEventFile().setStatus(CricketFunctions.findConsecutiveDupicateEvents(session_match.getEventFile().getEvents(), this_event));
						session_match.getEventFile().getEvents().add(this_event);
						break;
					}
				}
			}
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);

			return objectMapper.writeValueAsString(session_match);
			
		case CricketUtil.LOG_OVERWRITE_TEAM_TOTAL: case CricketUtil.LOG_OVERWRITE_TEAM_EXTRAS: 
		case CricketUtil.LOG_OVERWRITE_BATSMAN_STATS: case CricketUtil.LOG_OVERWRITE_BOWLER_FIGURES: 
		case CricketUtil.LOG_OVERWRITE_BATSMAN_HOWOUT: case CricketUtil.LOG_OVERWRITE_PARTNERSHIPS:
		case CricketUtil.LOG_OVERWRITE_BATTINGCARD: 
			
			int inningNum = 0;
			switch (whatToProcess.toUpperCase()) {
			case CricketUtil.LOG_OVERWRITE_TEAM_TOTAL: case CricketUtil.LOG_OVERWRITE_TEAM_EXTRAS: 
			case CricketUtil.LOG_OVERWRITE_BATSMAN_STATS: case CricketUtil.LOG_OVERWRITE_BOWLER_FIGURES: 
			case CricketUtil.LOG_OVERWRITE_BATSMAN_HOWOUT: case CricketUtil.LOG_OVERWRITE_PARTNERSHIPS:
			case CricketUtil.LOG_OVERWRITE_BATTINGCARD:
				inningNum = Integer.valueOf(valueToProcess.split(",")[valueToProcess.split(",").length-1]);
				break;
			default:
				this_inn = session_match.getMatch().getInning().stream().filter(inn -> 
					inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
				if(this_inn != null) {
					inningNum = this_inn.getInningNumber();
				} else {
					inningNum = 1;
				}
				break;
			}
			
			for(Inning inn:session_match.getMatch().getInning()) {
				if(inn.getInningNumber() == inningNum) {

					if(session_match.getEventFile() == null)
						session_match.setEventFile(new EventFile());
					if(session_match.getEventFile().getEvents() == null)
						session_match.getEventFile().setEvents(new ArrayList<Event>());
					this_event.setEventNumber(session_match.getEventFile().getEvents().size() + 1);
					this_event.setEventType(whatToProcess);
					this_event.setEventInningNumber(inn.getInningNumber());
					
					switch (whatToProcess.toUpperCase()) {
					case CricketUtil.LOG_OVERWRITE_BATTINGCARD:
						
						if(valueToProcess.split(",").length >= 4 && valueToProcess.split(",")[2].equalsIgnoreCase(CricketUtil.YES)) {
							this_event.setEventBattingCard(inn.getBattingCard().stream().filter(bc -> bc.getPlayerId() 
								== Integer.valueOf(valueToProcess.split(",")[0])).findFirst().orElse(null));
							this_event.setEventBatterPosition(IntStream.range(0, inn.getBattingCard().size())
						    	.filter(i -> inn.getBattingCard().get(i).getPlayerId() == Integer.valueOf(valueToProcess.split(",")[0]))
	                            .findFirst().orElse(-1));
							bc_itr = inn.getBattingCard().iterator();
							while(bc_itr.hasNext()) {
								this_bc = bc_itr.next();
								if(this_bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[0])) {
									bc_itr.remove();
									break;
								}
							}	
							
						} else { //Swap batsmen
							
						    firstBatter = IntStream.range(0, inn.getBattingCard().size())
	                              .filter(i -> inn.getBattingCard().get(i).getPlayerId() == Integer.valueOf(valueToProcess.split(",")[0]))
	                              .findFirst().orElse(-1);

						    secondBatter = IntStream.range(0, inn.getBattingCard().size())
	                              .filter(i -> inn.getBattingCard().get(i).getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1]))
	                              .findFirst().orElse(-1);							
							
					        if (firstBatter >= 0 && secondBatter >= 0) {
					            Collections.swap(inn.getBattingCard(), firstBatter, secondBatter);
					            this_event.setEventBatterPosition(firstBatter);
					            this_event.setEventBatterPreviousPosition(secondBatter);
					        }						    
						}
						this_event.setEventBatterNo(Integer.valueOf(valueToProcess.split(",")[0]));
						this_event.setEventOtherBatterNo(Integer.valueOf(valueToProcess.split(",")[1]));
						this_event.setEventExtra(valueToProcess);

						inn.getBattingCard().sort(Comparator.comparingInt(BattingCard::getBatterPosition));
						
						IntStream.range(0, inn.getBattingCard().size())
							.forEach(i -> inn.getBattingCard().get(i).setBatterPosition(i + 1));

						inn.getBattingCard().sort(Comparator.comparingInt(BattingCard::getBatterPosition));
						break;
						
					case CricketUtil.LOG_OVERWRITE_PARTNERSHIPS:

						for(Partnership part : inn.getPartnerships()) {
							if(part.getPartnershipNumber() == Integer.valueOf(valueToProcess.split(",")[0])) {
								
								this_event.setEventPartnership(new Partnership(part.getPartnershipNumber(), 
									part.getFirstBatterNo(), part.getSecondBatterNo(), part.getFirstBatterRuns(), 
									part.getSecondBatterRuns(), part.getFirstBatterBalls(), 
									part.getSecondBatterBalls(), part.getTotalRuns(), 
									part.getTotalBalls(), part.getTotalFours(), part.getTotalSixes()));
								
								part.setFirstBatterRuns(Integer.valueOf(valueToProcess.split(",")[1]));
								part.setSecondBatterRuns(Integer.valueOf(valueToProcess.split(",")[2]));
								part.setFirstBatterBalls(Integer.valueOf(valueToProcess.split(",")[3]));
								part.setSecondBatterBalls(Integer.valueOf(valueToProcess.split(",")[4]));
								part.setTotalRuns(Integer.valueOf(valueToProcess.split(",")[5]));
								part.setTotalBalls(Integer.valueOf(valueToProcess.split(",")[6]));
								part.setTotalFours(Integer.valueOf(valueToProcess.split(",")[7]));
								part.setTotalSixes(Integer.valueOf(valueToProcess.split(",")[8]));
								
								if(session_match.getSetup().getSpecialMatchRules() != null 
									&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
									this_event.getEventPartnership().setTotalNines(Integer.valueOf(valueToProcess.split(",")[9]));
									part.setTotalNines(Integer.valueOf(valueToProcess.split(",")[9]));
								}
							}
						}
						break;
						
					case CricketUtil.LOG_OVERWRITE_BATSMAN_STATS:

						for(BattingCard bc : inn.getBattingCard()) {
							if(bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[0])) {
								this_event.setEventBattingCard(new BattingCard(bc.getPlayerId(), bc.getRuns(), 
										bc.getFours(), bc.getSixes(), bc.getBalls()));
								bc.setRuns(Integer.valueOf(valueToProcess.split(",")[1]));
								bc.setBalls(Integer.valueOf(valueToProcess.split(",")[2]));
								bc.setFours(Integer.valueOf(valueToProcess.split(",")[3]));
								bc.setSixes(Integer.valueOf(valueToProcess.split(",")[4]));
								if(valueToProcess.split(",").length > 5) {
									bc.setOnStrike(valueToProcess.split(",")[5].toUpperCase());
								} else {
									bc.setOnStrike("");
								}
								bc.setStrikeRate(CricketFunctions.generateStrikeRate(Integer.valueOf(valueToProcess.split(",")[1]),
									Integer.valueOf(valueToProcess.split(",")[2]),1));
								bc.setDuration(Long.valueOf(TimeUnit.MINUTES.toSeconds(
									Integer.valueOf(valueToProcess.split(",")[6]))).intValue());
								if(session_match.getSetup().getSpecialMatchRules() != null 
									&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
									this_event.getEventBattingCard().setNines(Integer.valueOf(valueToProcess.split(",")[7]));
									bc.setNines(Integer.valueOf(valueToProcess.split(",")[7]));
								}
							}
						}
						break;
					
					case CricketUtil.LOG_OVERWRITE_BATSMAN_HOWOUT:

						for(BattingCard bc : inn.getBattingCard()) {
							if(bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[0])) {
								this_event.setEventBattingCard(objectMapper.readValue(objectMapper.writeValueAsString(bc), BattingCard.class));
								switch (valueToProcess.split(",")[1].toUpperCase()) {
								case CricketUtil.RETIRED_HURT:
									bc.setStatus(CricketUtil.STILL_TO_BAT);
									break;
								default:
									bc.setStatus(CricketUtil.OUT);
									break;
								}
								bc.setHowOut(valueToProcess.split(",")[1]);
								this_event.setEventHowOut(valueToProcess.split(",")[1]);
								bc.setHowOutFielderId(Integer.valueOf(valueToProcess.split(",")[2]));
								this_event.setEventHowOutFielderId(Integer.valueOf(valueToProcess.split(",")[2]));
								bc.setHowOutBowlerId(Integer.valueOf(valueToProcess.split(",")[3]));
								if(valueToProcess.split(",").length >= 4 && !valueToProcess.split(",")[4].isEmpty()) {
									bc.setConcussionPlayerId(Integer.valueOf(valueToProcess.split(",")[4]));
									this_event.setEventConcussionReplacePlayerId(Integer.valueOf(valueToProcess.split(",")[4]));
								}
								bc.setWasHowOutFielderSubstitute(valueToProcess.split(",")[5]);
								this_event.setSubstitutionMade(valueToProcess.split(",")[5]);
								bc=CricketFunctions.processBattingcard(cricketService, bc);
							}
						}
						break;
						
					case CricketUtil.LOG_OVERWRITE_BOWLER_FIGURES:

						this_bwc = inn.getBowlingCard().stream().filter(bc -> bc.getPlayerId() 
							== Integer.valueOf(valueToProcess.split(",")[0])).findAny().orElse(null);
						
						if(this_bwc != null) {
							this_event.setEventBowlingCard(this_bwc);
							this_event.setEventExtra(valueToProcess);
							if(valueToProcess.split(",").length >= 12 && valueToProcess.split(",")[10].equalsIgnoreCase(CricketUtil.YES)) {
								this_event.setEventBowlerNo(IntStream.range(0, inn.getBowlingCard().size())
							    	.filter(i -> inn.getBowlingCard().get(i).getPlayerId() == Integer.valueOf(valueToProcess.split(",")[0]))
		                            .findFirst().orElse(-1));
								inn.getBowlingCard().remove(this_bwc);
							} else {
								this_event.getEventBowlingCard().setStatus(valueToProcess.split(",")[9]);
								this_bwc.setOvers(Integer.valueOf(valueToProcess.split(",")[1]));
								this_bwc.setBalls(Integer.valueOf(valueToProcess.split(",")[2]));
								this_bwc.setRuns(Integer.valueOf(valueToProcess.split(",")[3]));
								this_bwc.setWickets(Integer.valueOf(valueToProcess.split(",")[4]));
								this_bwc.setWides(Integer.valueOf(valueToProcess.split(",")[5]));
								this_bwc.setNoBalls(Integer.valueOf(valueToProcess.split(",")[6]));
								this_bwc.setDots(Integer.valueOf(valueToProcess.split(",")[7]));
								this_bwc.setMaidens(Integer.valueOf(valueToProcess.split(",")[8]));
								this_bwc.setStatus(valueToProcess.split(",")[9]);
								this_bwc.setEconomyRate(CricketFunctions.generateRunRate(this_bwc.getRuns(),this_bwc.getOvers(), 
									this_bwc.getBalls(),2,session_match));
							}
						}
						break;
						
					case CricketUtil.LOG_OVERWRITE_TEAM_TOTAL: 
						
						this_event.setEventRuns(inn.getTotalRuns());
						this_event.setEventOverNo(inn.getTotalOvers());
						this_event.setEventBallNo(inn.getTotalBalls());
						this_event.setEventWickets(inn.getTotalWickets());
						this_event.setEventFours(inn.getTotalFours());
						this_event.setEventSixes(inn.getTotalSixes());

						inn.setTotalRuns(Integer.valueOf(valueToProcess.split(",")[0]));
						inn.setTotalWickets(Integer.valueOf(valueToProcess.split(",")[1]));
						inn.setTotalOvers(Integer.valueOf(valueToProcess.split(",")[2]));
						inn.setTotalBalls(Integer.valueOf(valueToProcess.split(",")[3]));
						inn.setTotalFours(Integer.valueOf(valueToProcess.split(",")[4]));
						inn.setTotalSixes(Integer.valueOf(valueToProcess.split(",")[5]));
						inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(), 2, session_match));
						if(session_match.getSetup().getSpecialMatchRules() != null 
							&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
						
							inn.setSpecialRuns(valueToProcess.split(",")[6].trim());
							if(inn.getSpecialRuns() != null && !inn.getSpecialRuns().isEmpty() && !inn.getSpecialRuns().equalsIgnoreCase("0") 
								&& !inn.getSpecialRuns().trim().contains("-")) {
								inn.setSpecialRuns("+" + inn.getSpecialRuns());
							}
							this_event.setEventExtra(inn.getSpecialRuns());

							inn.setTotalNines(Integer.valueOf(valueToProcess.split(",")[7]));
							this_event.setEventExtraRuns(inn.getTotalNines());
						
						}

						this_bwc.setRuns(Integer.valueOf(valueToProcess.split(",")[0]));
						this_bwc.setWickets(Integer.valueOf(valueToProcess.split(",")[1]));
						this_bwc.setOvers(Integer.valueOf(valueToProcess.split(",")[2]));
						this_bwc.setBalls(Integer.valueOf(valueToProcess.split(",")[3]));
						this_bc.setFours(Integer.valueOf(valueToProcess.split(",")[4]));
						this_bc.setSixes(Integer.valueOf(valueToProcess.split(",")[5]));
						if(session_match.getSetup().getSpecialMatchRules() != null 
							&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
							this_bc.setNines(Integer.valueOf(valueToProcess.split(",")[7]));
						}
						
						this_event.setEventBattingCard(this_bc);
						this_event.setEventBowlingCard(this_bwc);
						
						break;
						
					case CricketUtil.LOG_OVERWRITE_TEAM_EXTRAS:
						
						this_event.setEventExtra(String.valueOf(inn.getTotalWides()) + "," + String.valueOf(inn.getTotalNoBalls()) 
							+ "," + String.valueOf(inn.getTotalByes()) + "," + String.valueOf(inn.getTotalLegByes()) 
							+ "," + String.valueOf(inn.getTotalPenalties()) + "," + String.valueOf(inn.getTotalExtras()));
						
						inn.setTotalWides(Integer.valueOf(valueToProcess.split(",")[0]));
						inn.setTotalNoBalls(Integer.valueOf(valueToProcess.split(",")[1]));
						inn.setTotalByes(Integer.valueOf(valueToProcess.split(",")[2]));
						inn.setTotalLegByes(Integer.valueOf(valueToProcess.split(",")[3]));
						inn.setTotalPenalties(Integer.valueOf(valueToProcess.split(",")[4]));
						inn.setTotalExtras(Integer.valueOf(valueToProcess.split(",")[5]));
						
						break;
						
					}
					if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
						session_match.getMatch().setMatchStatus(CricketFunctions.GenerateMatchSummaryStatus(
							inn.getInningNumber(), session_match, "", "", "", false).getTargetOrResult());
					}
				}
			}

			switch (whatToProcess.toUpperCase()) {
			case CricketUtil.LOG_OVERWRITE_BATSMAN_HOWOUT:
				switch (valueToProcess.split(",")[1].toUpperCase()) {
				case CricketUtil.CONCUSSED:
					this_event.setEventHowOut(valueToProcess.split(",")[1].toUpperCase());
					for(Inning inn:session_match.getMatch().getInning()) {
						if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
							inn.getBattingCard().add(CricketFunctions.processBattingcard(cricketService, 
									new BattingCard(Integer.valueOf(valueToProcess.split(",")[4]),
									inn.getBattingCard().size() + 1, CricketUtil.STILL_TO_BAT)));
							this_event.setEventConcussionReplacePlayerId(Integer.valueOf(valueToProcess.split(",")[4]));
							//For TEST matches, add the replacement batsman to the next innings batting card of that team
			   				if(inn.getInningNumber() <= 2 && session_match.getMatch().getInning().size() >= 4) {
			   					//Keep looping in case of F/O
			   					for(int ing = 3; ing <= 4; ing++) {  
				   					if(session_match.getMatch().getInning().get(ing-1).getBattingTeamId() == inn.getBattingTeamId()) {
				   						session_match.getMatch().getInning().get(ing-1).getBattingCard().add(
				   							CricketFunctions.processBattingcard(cricketService, 
				   							new BattingCard(Integer.valueOf(valueToProcess.split(",")[4]),
											inn.getBattingCard().size() + 1, CricketUtil.STILL_TO_BAT)));
				   					}
			   					}
			   				}							
						}
					}
					break;
				}
			}
			
			session_match.getEventFile().setStatus(CricketFunctions.findConsecutiveDupicateEvents(session_match.getEventFile().getEvents(), this_event));
			session_match.getEventFile().getEvents().add(this_event);
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.SETUP + "," + CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");

			return objectMapper.writeValueAsString(session_match);
		}
		
		return objectMapper.writeValueAsString(session_match);
	}

	@RequestMapping(value = {"/processCricketProcedures"}, method={RequestMethod.GET,RequestMethod.POST})    
	public @ResponseBody String processCricketProcedures(
			@RequestParam(value = "whatToProcess", required = false, defaultValue = "") String whatToProcess,
			@RequestParam(value = "timeStatsToProcess", required = false, defaultValue = "") String timeStatsToProcess,
			@RequestParam(value = "valueToProcess", required = false, defaultValue = "") String valueToProcess) throws Exception
	{
		boolean lastBallOfTheOver = false, getSpeed = false;	
		Event this_event = new Event(whatToProcess, valueToProcess);
		Event challengeEvent = new Event();
		Iterator<BattingCard> bc_itr;
		DaySession thisDaySession = new DaySession();
		BattingCard this_bc = new BattingCard();
		
		switch (whatToProcess.toUpperCase()) {
		case CricketUtil.LOG_ANY_BALL: case CricketUtil.LOG_WICKET: case CricketUtil.LOG_EVENT:
			
			getSpeed = false;
			switch (whatToProcess.toUpperCase()) {
			case CricketUtil.LOG_EVENT:
				switch (valueToProcess.toUpperCase()) {
				case CricketUtil.DOT: case CricketUtil.ONE: case CricketUtil.TWO: case CricketUtil.THREE: 
				case CricketUtil.FIVE: case CricketUtil.NO_BALL:
					getSpeed = true;
					break;
				default:
					if(valueToProcess.toUpperCase().contains(CricketUtil.FOUR) || valueToProcess.toUpperCase().contains(CricketUtil.SIX)
						|| valueToProcess.toUpperCase().contains(CricketUtil.BYE) || valueToProcess.toUpperCase().contains(CricketUtil.LEG_BYE)
						|| valueToProcess.toUpperCase().contains(CricketUtil.PENALTY) || valueToProcess.toUpperCase().contains(CricketUtil.NINE)
						|| valueToProcess.toUpperCase().contains(CricketUtil.WIDE))
					getSpeed = true;
					break;
				}
				break;
			case CricketUtil.LOG_ANY_BALL: case CricketUtil.LOG_WICKET:
				getSpeed = true;
				break;
			}
			
			if(getSpeed == true) {
				if(session_match.getSetup().getSpeedFilePath() != null 
					&& !session_match.getSetup().getSpeedFilePath().isEmpty()) {
					session_speed = CricketFunctions.getCurrentSpeed(session_match.getSetup().getSpeedFilePath(), session_speed);
					if(session_speed != null) {
						session_match.getMatch().setCurrent_speed(session_speed.getSpeedValue());
					} else {
						session_speed = new Speed(0);
					}
				}
			}
			break;
		}		
		
		challengeEvent = null;
		if(session_match.getSetup().getSpecialMatchRules() != null 
			&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
			if(session_match.getEventFile().getEvents() != null && session_match.getEventFile().getEvents().size() > 0) {
				for(Inning inn : session_match.getMatch().getInning()) {
					if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
						challengeEvent = session_match.getEventFile().getEvents().stream().filter(
							evnt -> inn.getInningNumber() == evnt.getEventInningNumber() && evnt.getEventExtra() != null 
							&& evnt.getEventExtra().equalsIgnoreCase("CHALLENGE")).findFirst().orElse(null);
					}
				}
			}
		}
		
		switch (whatToProcess.toUpperCase()) {
		case CricketUtil.UNDO:

			if(session_match.getEventFile().getEvents() != null && Integer.valueOf(valueToProcess) <= session_match.getEventFile().getEvents().size()) {
				
				for(int iUndo=1;iUndo<=Integer.valueOf(valueToProcess);iUndo++) {
					
					this_event = session_match.getEventFile().getEvents().get(session_match.getEventFile().getEvents().size() - 1);

					switch (this_event.getEventType().toUpperCase()) {
					case "LOG_IMPACT":
						
						for (Player plyr : session_match.getSetup().getHomeSquad()) {
							if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
								plyr.setSubstitutionType("");
							}
						}
						for (Player plyr : session_match.getSetup().getHomeSubstitutes()) {
							if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
								plyr.setSubstitutionType("");
							}
						}
						for (Player plyr : session_match.getSetup().getHomeOtherSquad()) {
							if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
								plyr.setSubstitutionType("");
							}
						}
						for (Player plyr : session_match.getSetup().getAwaySquad()) {
							if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
								plyr.setSubstitutionType("");
							}
						}
						for (Player plyr : session_match.getSetup().getAwaySubstitutes()) {
							if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
								plyr.setSubstitutionType("");
							}
						}
						for (Player plyr : session_match.getSetup().getAwayOtherSquad()) {
							if(plyr.getPlayerId() == this_event.getEventBatterNo() || plyr.getPlayerId() == this_event.getEventOtherBatterNo()) {
								plyr.setSubstitutionType("");
							}
						}			
						if(session_match.getSetup().getSpecialMatchRules() != null 
							&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
						}
						else 
						{
							for(Inning inn:session_match.getMatch().getInning()) {
								if(inn.getInningNumber() == this_event.getEventInningNumber()) {
									bc_itr = inn.getBattingCard().iterator();
									while(bc_itr.hasNext()) {
										this_bc = bc_itr.next();
										if(this_bc.getPlayerId() == this_event.getEventBatterNo()) {
											bc_itr.remove();
											break;
										}
									}	
									if(this_event.getEventBattingCard() != null) {
										if(this_event.getEventBattingCard().getBatsmanInningStarted() != null 
											&& this_event.getEventBattingCard().getBatsmanInningStarted().equalsIgnoreCase(CricketUtil.YES)) {
										} else {
											inn.getBattingCard().add(this_event.getEventBattingCard());
										}
									}
									Collections.sort(inn.getBattingCard());
								}
							}		
						}
						break;
						
					case CricketUtil.SWAP_BATSMAN:

						for(Inning inn:session_match.getMatch().getInning()) {
							if(this_event.getEventInningNumber() == inn.getInningNumber()) {
								for(BattingCard bc:inn.getBattingCard()) {
									if(this_event.getEventBatterNo() == bc.getPlayerId()) {
										bc.setOnStrike(CricketUtil.YES);
									} 
									else if(this_event.getEventOtherBatterNo() == bc.getPlayerId()) {
										bc.setOnStrike(CricketUtil.NO);
									} 
								}
							}
						}
						break;
					
					case CricketUtil.LOG_OVERWRITE_TEAM_TOTAL: case CricketUtil.LOG_OVERWRITE_TEAM_EXTRAS: case CricketUtil.LOG_OVERWRITE_BATSMAN_STATS: 
					case CricketUtil.LOG_OVERWRITE_BOWLER_FIGURES: case CricketUtil.LOG_OVERWRITE_BATSMAN_HOWOUT: case CricketUtil.LOG_OVERWRITE_PARTNERSHIPS:
					case CricketUtil.LOG_OVERWRITE_BATTINGCARD: 

						for(Inning inn:session_match.getMatch().getInning()) {
							
							if(this_event.getEventInningNumber() == inn.getInningNumber()) {

								switch (this_event.getEventType().toUpperCase()) {
								case CricketUtil.LOG_OVERWRITE_BATTINGCARD:
									
									if(this_event.getEventExtra().split(",").length >= 4 
										&& this_event.getEventExtra().split(",")[2].equalsIgnoreCase(CricketUtil.YES)
										&& this_event.getEventBattingCard() != null && this_event.getEventBatterPosition() >= 0) 
									{
										inn.getBattingCard().add(this_event.getEventBatterPosition(), this_event.getEventBattingCard());
										
									} else { // Swap batsmen
										
								        if (this_event.getEventBatterPosition() >= 0 && this_event.getEventBatterPreviousPosition() >= 0) {
								            Collections.swap(inn.getBattingCard(), this_event.getEventBatterPosition(), 
								            	this_event.getEventBatterPreviousPosition());
								        }						    
									}
									
									inn.getBattingCard().sort(Comparator.comparingInt(BattingCard::getBatterPosition));
									
									IntStream.range(0, inn.getBattingCard().size())
										.forEach(i -> inn.getBattingCard().get(i).setBatterPosition(i + 1));

									inn.getBattingCard().sort(Comparator.comparingInt(BattingCard::getBatterPosition));
									break;
								
								case CricketUtil.LOG_OVERWRITE_TEAM_TOTAL: 
									
									inn.setTotalRuns(this_event.getEventRuns());
									inn.setTotalWickets(this_event.getEventWickets());
									inn.setTotalOvers(this_event.getEventOverNo());
									inn.setTotalBalls(this_event.getEventBallNo());
									inn.setTotalFours(this_event.getEventFours());
									inn.setTotalSixes(this_event.getEventSixes());
									inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(), 2, session_match));
									if(session_match.getSetup().getSpecialMatchRules() != null 
										&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
										inn.setSpecialRuns(this_event.getEventExtra());
										inn.setTotalNines(this_event.getEventExtraRuns());
									}
									
									break;

								case CricketUtil.LOG_OVERWRITE_TEAM_EXTRAS: 
									
									inn.setTotalWides(Integer.valueOf(this_event.getEventExtra().split(",")[0]));
									inn.setTotalNoBalls(Integer.valueOf(this_event.getEventExtra().split(",")[1]));
									inn.setTotalByes(Integer.valueOf(this_event.getEventExtra().split(",")[2]));
									inn.setTotalLegByes(Integer.valueOf(this_event.getEventExtra().split(",")[3]));
									inn.setTotalPenalties(Integer.valueOf(this_event.getEventExtra().split(",")[4]));
									inn.setTotalExtras(Integer.valueOf(this_event.getEventExtra().split(",")[5]));
									
									break;

								case CricketUtil.LOG_OVERWRITE_BATSMAN_STATS: 
									
									for(BattingCard bc : inn.getBattingCard()) {
										if(bc.getPlayerId() == this_event.getEventBattingCard().getPlayerId()) {
											bc.setRuns(this_event.getEventBattingCard().getRuns());
											bc.setBalls(this_event.getEventBattingCard().getBalls());
											bc.setFours(this_event.getEventBattingCard().getFours());
											bc.setSixes(this_event.getEventBattingCard().getSixes());
											bc.setStrikeRate(CricketFunctions.generateStrikeRate(this_event.getEventBattingCard().getRuns(),
												this_event.getEventBattingCard().getBalls(),1));
											if(session_match.getSetup().getSpecialMatchRules() != null 
												&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
												bc.setNines(this_event.getEventBattingCard().getNines());
											}
										}
									}
									break;
									
								case CricketUtil.LOG_OVERWRITE_BOWLER_FIGURES: 

									if(this_event.getEventBowlingCard() != null) {
										if(this_event.getEventExtra().split(",").length >= 12 
											&& this_event.getEventExtra().split(",")[10].equalsIgnoreCase(CricketUtil.YES)
											&& this_event.getEventBowlerNo() >= 0) {
											
											inn.getBowlingCard().add(this_event.getEventBowlerNo(),this_event.getEventBowlingCard());
											
										}else  {
											
											for(BowlingCard bc : inn.getBowlingCard()) {
												if(bc.getPlayerId() == this_event.getEventBowlingCard().getPlayerId()) {
													bc.setOvers(this_event.getEventBowlingCard().getOvers());
													bc.setBalls(this_event.getEventBowlingCard().getBalls());
													bc.setRuns(this_event.getEventBowlingCard().getRuns());
													bc.setWickets(this_event.getEventBowlingCard().getWickets());
													bc.setWides(this_event.getEventBowlingCard().getWides());
													bc.setNoBalls(this_event.getEventBowlingCard().getNoBalls());
													bc.setDots(this_event.getEventBowlingCard().getDots());
													bc.setMaidens(this_event.getEventBowlingCard().getMaidens());
													bc.setStatus(this_event.getEventBowlingCard().getStatus());
													bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(), bc.getBalls()
														,2, session_match));
												}
											}
										}
									}
									break;
									
								case CricketUtil.LOG_OVERWRITE_BATSMAN_HOWOUT: 
									
									for(BattingCard bc : inn.getBattingCard()) {
										if(bc.getPlayerId() == this_event.getEventBattingCard().getPlayerId()) {
											bc.setStatus(this_event.getEventBattingCard().getStatus());
											bc.setHowOut(this_event.getEventBattingCard().getHowOut());
											bc.setHowOutFielderId(this_event.getEventBattingCard().getHowOutFielderId());
											bc.setHowOutBowlerId(this_event.getEventBattingCard().getHowOutBowlerId());
											if(this_event.getEventBattingCard().getConcussionPlayerId() > 0) {
												bc.setConcussionPlayerId(this_event.getEventBattingCard().getConcussionPlayerId());
											}
											bc=CricketFunctions.processBattingcard(cricketService, bc);
										}
									}
									if(this_event.getEventHowOut() != null) {
										switch (this_event.getEventHowOut().toUpperCase()) {
										case CricketUtil.CONCUSSED:
											bc_itr = inn.getBattingCard().iterator();
											while(bc_itr.hasNext()) {
												this_bc = bc_itr.next();
												if(this_bc.getPlayerId() == this_event.getEventConcussionReplacePlayerId()) {
													bc_itr.remove();
													break;
												}
											}
											//For TEST matches remove replacement batsman from the next inning of the batting team
							   				if(inn.getInningNumber() <= 2 && session_match.getMatch().getInning().size() >= 4) {
							   					for(int ing = 3; ing <= 4; ing++) {
							   						if(session_match.getMatch().getInning().get(ing-1).getBattingTeamId() == inn.getBattingTeamId()) {
														bc_itr = session_match.getMatch().getInning().get(ing-1).getBattingCard().iterator();
														while(bc_itr.hasNext()) {
															this_bc = bc_itr.next();
															if(this_bc.getPlayerId() == this_event.getEventConcussionReplacePlayerId()) {
																bc_itr.remove();
																break;
															}
														}
							   						}
							   					}
							   				}
							   				if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
												session_match = CricketFunctions.populateMatchVariables(cricketService, session_match);
											}
											break;
										}
									}
									break;

								case CricketUtil.LOG_OVERWRITE_PARTNERSHIPS:
									
									for(Partnership part : inn.getPartnerships()) {
										if(part.getPartnershipNumber() == this_event.getEventPartnership().getPartnershipNumber()) {
											part.setFirstBatterRuns(this_event.getEventPartnership().getFirstBatterRuns());
											part.setSecondBatterRuns(this_event.getEventPartnership().getSecondBatterRuns());
											part.setFirstBatterBalls(this_event.getEventPartnership().getFirstBatterBalls());
											part.setSecondBatterBalls(this_event.getEventPartnership().getSecondBatterBalls());
											part.setTotalRuns(this_event.getEventPartnership().getTotalRuns());
											part.setTotalBalls(this_event.getEventPartnership().getTotalBalls());
											part.setTotalFours(this_event.getEventPartnership().getTotalFours());
											part.setTotalSixes(this_event.getEventPartnership().getTotalSixes());
											if(session_match.getSetup().getSpecialMatchRules() != null 
												&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
												part.setTotalNines(this_event.getEventPartnership().getTotalNines());
											}
										}
									}
									break;

								}
							}
						}

						break;
					
					case CricketUtil.RESULT:
					
						session_match.getMatch().setMatchResult("");
						break;

					case CricketUtil.LOG_REVIEW:
						
						for(Inning inn:session_match.getMatch().getInning()) {
							if(inn.getInningNumber() == this_event.getEventInningNumber()) {
								Iterator<Review> rev_itr = inn.getReviews().iterator();
								while(rev_itr.hasNext()) {
									if(rev_itr.next().getReviewNumber() == this_event.getEventStatNumber()) {
										rev_itr.remove();
									}
								}
							}
						}
						break;
						
					case CricketUtil.FOUR: case CricketUtil.SIX: case CricketUtil.NINE:
						
						for(Inning inn:session_match.getMatch().getInning()) {
							if(inn.getInningNumber() == this_event.getEventInningNumber()) {
								for(BowlingCard bc:inn.getBowlingCard()) {
									if(this_event.getEventBowlerNo() == bc.getPlayerId()) {
										if(this_event.getEventBallNo() <= Integer.valueOf(CricketUtil.DOT)) {
											inn.setTotalBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
											inn.setTotalOvers(inn.getTotalOvers() - 1);
											lastBallOfTheOver = true;
										} else {
											inn.setTotalBalls(inn.getTotalBalls() - 1);
										}
										if(this_event.getEventBallNo() <= Integer.valueOf(CricketUtil.DOT)) {
											bc.setBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
											bc.setOvers(bc.getOvers() - 1);
										} else {
											bc.setBalls(bc.getBalls() - 1);
										}
										if(session_match.getSetup().getSpeedFilePath() != null 
												&& !session_match.getSetup().getSpeedFilePath().isEmpty()) {
											if(bc.getSpeeds() != null && bc.getSpeeds().size() > 0) {
												bc.getSpeeds().removeIf(sp -> sp.getOverNumber() == bc.getOvers() && sp.getBallNumber() == bc.getBalls());
											}
										}
										bc.setRuns(bc.getRuns() - this_event.getEventRuns());
										bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() - this_event.getEventRuns());
										bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
										inn.setTotalRuns(inn.getTotalRuns() - this_event.getEventRuns());
										if(session_match.getSetup().getSpecialMatchRules() != null 
											&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
											if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
												if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
													[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
													|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
												{
													if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
														if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
															inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
														} 
//														else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//															inn.setSpecialRuns("0");
//														} 
														else {
															inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
														}
													}
												}
											}
										}												
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() - this_event.getEventRuns());
										for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
											if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
												inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() - 1);
												inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() - this_event.getEventRuns());
											}
										}
										if(session_match.getMatch().getDaysSessions() != null) {
											for(DaySession ds : session_match.getMatch().getDaysSessions()) {
												if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
													ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
													ds.setTotalBalls(ds.getTotalBalls() - 1);
													switch (this_event.getEventType().toUpperCase()) {
													case CricketUtil.FOUR: 
														ds.setTotalFours(ds.getTotalFours() - 1);
														for(DaySession bds : bc.getBowlingSession()) {
															if(ds.getDayNumber() == bds.getDayNumber() 
																&& ds.getSessionNumber() == bds.getSessionNumber()) {
																bds.setTotalRuns(bds.getTotalRuns() - this_event.getEventRuns());
																bds.setTotalBalls(bds.getTotalBalls() - 1);
																bds.setTotalFours(bds.getTotalFours() - 1);
															}
														}
														break;
													case CricketUtil.SIX:
														ds.setTotalSixes(ds.getTotalSixes() - 1);
														for(DaySession bds : bc.getBowlingSession()) {
															if(ds.getDayNumber() == bds.getDayNumber() 
																&& ds.getSessionNumber() == bds.getSessionNumber()) {
																bds.setTotalRuns(bds.getTotalRuns() - this_event.getEventRuns());
																bds.setTotalBalls(bds.getTotalBalls() - 1);
																bds.setTotalSixes(bds.getTotalSixes() - 1);
															}
														}
														break;
													}
												}
											}
										}
										inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));
									}
								}
								
								for(BattingCard bc:inn.getBattingCard()) {
									if(this_event.getEventBatterNo() == bc.getPlayerId()) {
										bc.setRuns(bc.getRuns() - this_event.getEventRuns());
										bc.setBalls(bc.getBalls() - 1);
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBattingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalBalls(ds.getTotalBalls() - 1);
														ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
													}
												}
											}
										}
										bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
										if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() - this_event.getEventRuns());
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() - 1);
										} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() - this_event.getEventRuns());
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() - 1);
										}
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() - 1);										
										if(this_event.getEventWasABoundary() != null && this_event.getEventWasABoundary().equalsIgnoreCase(CricketUtil.YES)) {
											switch (this_event.getEventType().toUpperCase()) {
											case CricketUtil.FOUR: 
												bc.setFours(bc.getFours() - 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBattingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalFours(ds.getTotalFours() - 1);
															}
														}
													}
												}
												inn.setTotalFours(inn.getTotalFours() - 1);												
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalFours(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalFours() - 1);
												break;
											case CricketUtil.SIX:
												bc.setSixes(bc.getSixes() - 1);
												inn.setTotalSixes(inn.getTotalSixes() - 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBattingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalSixes(ds.getTotalSixes() - 1);
															}
														}
													}
												}
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalSixes(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalSixes() - 1);
												break;
											case CricketUtil.NINE:
												bc.setNines(bc.getNines() - 1);
												inn.setTotalNines(inn.getTotalNines() - 1);
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalNines(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalNines() - 1);
												break;
											}
										}
										if(lastBallOfTheOver == true) {
											bc.setOnStrike(CricketUtil.YES);
										}
									} else if(this_event.getEventOtherBatterNo() == bc.getPlayerId()) {
										if(lastBallOfTheOver == true) {
											bc.setOnStrike(CricketUtil.NO);
										}
									}
								} 
							} 
						} 
						if(session_match.getMatch().getWagons() != null && session_match.getMatch().getWagons().size() > 0) {
							session_match.getMatch().getWagons().remove(session_match.getMatch().getWagons().size()-1);
						}
						if(session_match.getMatch().getShots() != null && session_match.getMatch().getShots().size() > 0) {
							session_match.getMatch().getShots().remove(session_match.getMatch().getShots().size()-1);
						}
						break;
						
					case CricketUtil.LEG_BYE: case CricketUtil.BYE:

						for(Inning inn:session_match.getMatch().getInning()) {
							if(inn.getInningNumber() == this_event.getEventInningNumber()) {
								for(BowlingCard bc:inn.getBowlingCard()) {
									if(bc.getPlayerId() == this_event.getEventBowlerNo()) {
										if(this_event.getEventBallNo() <= Integer.valueOf(CricketUtil.DOT)) {
											inn.setTotalBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
											inn.setTotalOvers(inn.getTotalOvers() - 1);
											lastBallOfTheOver = true;
										} else {
											inn.setTotalBalls(inn.getTotalBalls() - 1);
										}
										if(this_event.getEventBallNo() <= Integer.valueOf(CricketUtil.DOT)) {
											bc.setBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
											bc.setOvers(bc.getOvers() - 1);
										} else {
											bc.setBalls(bc.getBalls() - 1);
										}
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalBalls(ds.getTotalBalls() - 1);
													}
												}
											}
										}													
										if(session_match.getSetup().getSpeedFilePath() != null && !session_match.getSetup().getSpeedFilePath().isEmpty()) {
											if(bc.getSpeeds() != null && bc.getSpeeds().size() > 0) {
												bc.getSpeeds().removeIf(sp -> sp.getOverNumber() == bc.getOvers() && sp.getBallNumber() == bc.getBalls());
											}
										}
										bc.setDots(bc.getDots() - 1);
										bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
										inn.setTotalRuns(inn.getTotalRuns() - this_event.getEventRuns());
										if(session_match.getSetup().getSpecialMatchRules() != null 
											&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
											if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
												if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
													[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
													|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
												{
													if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
														if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
															inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
														} 
//														else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//															inn.setSpecialRuns("0");
//														} 
														else {
															inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
														}
													}
												}
											}
										}													
										
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() - this_event.getEventRuns());
										for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
											if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
												inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() - 1);
											}
										}
										if(session_match.getMatch().getDaysSessions() != null) {
											for(DaySession ds : session_match.getMatch().getDaysSessions()) {
												if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
													ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
													ds.setTotalBalls(ds.getTotalBalls() - 1);
												}
											}
										}
										inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));
										if(this_event.getEventType().toUpperCase().contains(CricketUtil.LEG_BYE)) {
											inn.setTotalLegByes(inn.getTotalLegByes() - this_event.getEventExtraRuns());
										} else if(this_event.getEventType().toUpperCase().contains(CricketUtil.BYE)) {
											inn.setTotalByes(inn.getTotalByes() - this_event.getEventExtraRuns());
										}
										inn.setTotalExtras(inn.getTotalWides() + inn.getTotalNoBalls() + inn.getTotalByes() + inn.getTotalLegByes() + inn.getTotalPenalties());
									}
								}
								for(BattingCard bc:inn.getBattingCard()) {
									if(bc.getPlayerId() == this_event.getEventBatterNo()) {
										bc.setBalls(bc.getBalls() - 1);
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBattingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalBalls(ds.getTotalBalls() - 1);
													}
												}
											}
										}
										bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
										if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() - 1);
										} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() - 1);
										}
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() - 1);										
										if(lastBallOfTheOver == true) {
											switch (String.valueOf(this_event.getEventRuns())) {
											case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: case CricketUtil.NINE: 
												bc.setOnStrike(CricketUtil.YES);
												break;
											}
										} else {
											switch (String.valueOf(this_event.getEventRuns())) {
											case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
												bc.setOnStrike(CricketUtil.YES);
												break;
											}
										}
									} else if(bc.getPlayerId() == this_event.getEventOtherBatterNo()) {
										if(lastBallOfTheOver == true) {
											switch (String.valueOf(this_event.getEventRuns())) {
											case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: case CricketUtil.NINE:
												bc.setOnStrike(CricketUtil.NO);
												break;
											}
										} else {
											switch (String.valueOf(this_event.getEventRuns())) {
											case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
												bc.setOnStrike(CricketUtil.NO);
												break;
											}
										}
									}
								}
							}
						}
						break;

					case CricketUtil.NEW_BATSMAN:
					
						for(Inning inn:session_match.getMatch().getInning()) {
							if(inn.getInningNumber() == this_event.getEventInningNumber()) {
								final Integer this_player_id = this_event.getEventBatterNo();
								for(BattingCard bc:inn.getBattingCard()) {
									if(bc.getBatterPosition() == this_event.getEventBatterPreviousPosition())
										bc.setBatterPosition(this_event.getEventBatterPosition());
								}						
								for(BattingCard bc:inn.getBattingCard()) {
									if(bc.getPlayerId() == this_event.getEventBatterNo()) {
										bc.setBattingSession(new ArrayList<DaySession>());
										bc.setStatus(CricketUtil.STILL_TO_BAT);
										bc.setBatterPosition(this_event.getEventBatterPreviousPosition());
										bc.setOnStrike(CricketUtil.NO);
										if(this_event.getEventHowOut() != null && (this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.RETIRED_HURT))) {
											bc.setHowOut(this_event.getEventHowOut());
											bc.setBatsmanInningStarted(CricketUtil.YES);
										} else {
											bc.setBatsmanInningStarted(CricketUtil.NO);
										}
									}
								}
								if(this_event.getSubstitutionMade() != null && !this_event.getSubstitutionMade().isEmpty()) {
									inn.getBattingCard().removeIf(bc -> bc.getPlayerId() == this_player_id);
									if(inn.getBattingTeamId() == session_match.getSetup().getHomeTeamId()) {
										if(this_event.getEventBattingCard() != null && this_event.getEventBattingCard().getPlayer() != null) {
											session_match.getSetup().getHomeSubstitutes().add(this_event.getEventBattingCard().getPlayer());
										} else {
											session_match.getSetup().getHomeSubstitutes().add(CricketFunctions.populatePlayer(cricketService, 
												new Player(this_event.getEventBatterNo(), session_match.getSetup().getHomeSubstitutes().size() + 1)
												, session_match));
										}
									}else if(inn.getBattingTeamId() == session_match.getSetup().getAwayTeamId()) {
										if(this_event.getEventBattingCard() != null && this_event.getEventBattingCard().getPlayer() != null) {
											session_match.getSetup().getAwaySubstitutes().add(this_event.getEventBattingCard().getPlayer());
										} else {
											session_match.getSetup().getAwaySubstitutes().add(CricketFunctions.populatePlayer(cricketService, 
												new Player(this_event.getEventBatterNo(), session_match.getSetup().getAwaySubstitutes().size() + 1)
												, session_match));
										}
									}
								}
								if(inn.getPartnerships() != null && inn.getPartnerships().size() > 0)
									inn.getPartnerships().remove(inn.getPartnerships().size() - 1);
							}						
						}						
						break;
					
					case CricketUtil.END_OVER:
					
						for(Inning inn:session_match.getMatch().getInning()) {
							if(inn.getInningNumber() == this_event.getEventInningNumber()) {
								if(inn.getBowlingCard() != null) {
									for(BowlingCard bc:inn.getBowlingCard()) {
										if(bc.getPlayerId() == this_event.getEventBowlerNo()) {
											bc.setStatus(CricketUtil.CURRENT + CricketUtil.BOWLER);
											if(this_event.getEventTotalRunsInAnOver() <= 0) {
												bc.setMaidens(bc.getMaidens() - 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBowlingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalMaidens(ds.getTotalMaidens() - 1);
															}
														}
													}
												}									
											}
											bc.setTotalRunsThisOver(this_event.getEventTotalRunsInAnOver());
											for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
												if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
													inn.getSpells().get(i).setMaidens(inn.getSpells().get(i).getMaidens() - 1);
												}
											}
										}
									}
								} 
							}
						}
						break;
						
					case CricketUtil.CHANGE_BOWLER:
						
						for(Inning inn:session_match.getMatch().getInning()) {
							if(inn.getInningNumber() == this_event.getEventInningNumber()) {
								if(inn.getBowlingCard() != null) {
									for(BowlingCard bc:inn.getBowlingCard()) {
										if(bc.getPlayerId() == this_event.getEventBowlerNo()) {
											bc.setStatus(CricketUtil.OTHER + CricketUtil.BOWLER);
											bc.setBowling_end(0);
											if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
												if(bc.getBallTypeOverNo().contains(",")) {
													List<String> ballTypeOverNoList = new LinkedList<String>(
														Arrays.asList(bc.getBallTypeOverNo().split(",")));
													ballTypeOverNoList.remove(ballTypeOverNoList.size()-1);
													bc.setBallTypeOverNo(String.join(",", ballTypeOverNoList));
												} else {
													bc.setBallTypeOverNo("");
												}
											}
										} else if(bc.getPlayerId() == this_event.getEventOtherBowlerNo()) {
											bc.setStatus(CricketUtil.LAST + CricketUtil.BOWLER);
											bc.setBowling_end(this_event.getEventBowlingEnd());
										}
									}
									inn.getBowlingCard().removeIf( // remove bowler from bowling card if he hasn't bowled a single delivery
										(BowlingCard bc) -> bc.getOvers() <= 0 && bc.getBalls() <= 0 
										&& bc.getWides() <= 0 && bc.getNoBalls() <= 0
										&& bc.getRuns() <= 0 && bc.getWickets() <= 0);
								} 
								if(inn.getSpells() != null) {
									if(inn.getSpells().size() <= 1) {
										inn.setSpells(new ArrayList<Spell>());
									} else {
										int spell_index = -1;
										for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
											if(inn.getSpells().get(i).getPlayerId() == this_event.getEventBowlerNo() 
												&& inn.getSpells().get(i).getBalls() <= 0) {
												spell_index = i;
											}
										}
										if(spell_index >= 0) {
											inn.getSpells().remove(spell_index);
										}
									}
								} 
							}
						}
						break;
						
					case CricketUtil.LOG_ANY_BALL:
						
						for(Inning inn:session_match.getMatch().getInning()) {
							if(inn.getInningNumber() == this_event.getEventInningNumber()) {
								for(BowlingCard bc:inn.getBowlingCard()) {
									if(bc.getPlayerId() == this_event.getEventBowlerNo()) 
									{
										if(this_event.getEventHowOut() != null) {
											switch (this_event.getEventHowOut().toUpperCase()) {
											case CricketUtil.CAUGHT_AND_BOWLED: case CricketUtil.CAUGHT: case CricketUtil.BOWLED: case CricketUtil.STUMPED: 
											case CricketUtil.LBW: case CricketUtil.HIT_WICKET:
												bc.setWickets(bc.getWickets() - 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBowlingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalWickets(ds.getTotalWickets() - 1);
															}
														}
													}
												}													
												if(this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.CAUGHT_AND_BOWLED))
													bc.setCatchAsBowler(bc.getCatchAsBowler() - 1);
												break;
											}										
										}
										bc.setRuns(bc.getRuns() - this_event.getEventRuns());
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
													}
												}
											}
										}													
										//If any ball is not a wide nor no-ball, then check if its a bye or a leg-bye with run out
										//which will then get counted as a dot ball for the bowler (Arpit 25th May 2025)
										if(this_event.getEventExtra() == null || this_event.getEventExtra().trim().isEmpty()) { 
											if(this_event.getEventSubExtra() != null && !this_event.getEventSubExtra().isEmpty()) {
												if(this_event.getEventHowOut() != null && !this_event.getEventHowOut().isEmpty()) {
													switch (this_event.getEventSubExtra().toUpperCase()) {
													case CricketUtil.BYE: case CricketUtil.LEG_BYE:
														switch (this_event.getEventHowOut().toUpperCase()) {
														case CricketUtil.RUN_OUT: case CricketUtil.OBSTRUCTING_FIELDER: 
															bc.setDots(bc.getDots() - 1);
															break;
														}
														break;
													}
												}
											} else {
												if(this_event.getEventRuns() <= 0 && this_event.getEventExtraRuns() <= 0 && this_event.getEventSubExtraRuns() <= 0) {
													bc.setDots(bc.getDots() - 1);
												}
											}
										}
										
										for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
											if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
												inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() - this_event.getEventRuns());
											}
										}
										if(this_event.getEventExtra() != null) {
											switch (this_event.getEventExtra().toUpperCase()) {
											case CricketUtil.WIDE: case CricketUtil.NO_BALL:
												switch (this_event.getEventExtra().toUpperCase()) {
												case CricketUtil.WIDE:
													bc.setRuns(bc.getRuns() - 1);
													if(session_match.getMatch().getDaysSessions() != null) {
														thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
															ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
														if(thisDaySession != null) {
															for(DaySession ds : bc.getBowlingSession()) {
																if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																	&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																	ds.setTotalRuns(ds.getTotalRuns() - 1);
																}
															}
														}
													}													
													bc.setWides(bc.getWides() - 1);
													inn.setTotalWides(inn.getTotalWides() - 1);
													break;
												case CricketUtil.NO_BALL: 
													bc.setRuns(bc.getRuns() - Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
													if(session_match.getMatch().getDaysSessions() != null) {
														thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
															ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
														if(thisDaySession != null) {
															for(DaySession ds : bc.getBowlingSession()) {
																if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																	&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																	ds.setTotalRuns(ds.getTotalRuns() - Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
																}
															}
														}
													}													
													bc.setNoBalls(bc.getNoBalls() - 1);
													inn.setTotalNoBalls(inn.getTotalNoBalls() - 1);
													break;
												}
												for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
													if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
														inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() - 1);
													}
												}
												break;
											}
										}
										if(this_event.getEventSubExtra() != null) {
											switch (this_event.getEventSubExtra().toUpperCase()) {
											case CricketUtil.WIDE: case CricketUtil.NO_BALL:
												bc.setRuns(bc.getRuns() - this_event.getEventSubExtraRuns());
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBowlingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventSubExtraRuns());
															}
														}
													}
												}													
												switch (this_event.getEventSubExtra().toUpperCase()) {
												case CricketUtil.WIDE:
													bc.setWides(bc.getWides() - this_event.getEventSubExtraRuns());
													inn.setTotalWides(inn.getTotalWides() - this_event.getEventSubExtraRuns());
													break;
												case CricketUtil.NO_BALL: 
													bc.setNoBalls(bc.getNoBalls() - this_event.getEventSubExtraRuns());
													inn.setTotalNoBalls(inn.getTotalNoBalls() - this_event.getEventSubExtraRuns());
													break;
												}
												for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
													if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
														inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() - this_event.getEventSubExtraRuns());
													}
												}
												break;
											default:
												switch (this_event.getEventSubExtra().toUpperCase()) {
												case CricketUtil.BYE: 
													inn.setTotalByes(inn.getTotalByes() - this_event.getEventSubExtraRuns());
													break;
												case CricketUtil.LEG_BYE:
													inn.setTotalLegByes(inn.getTotalLegByes() - this_event.getEventSubExtraRuns());
													break;
												case CricketUtil.PENALTY: 
													inn.setTotalPenalties(inn.getTotalPenalties() - this_event.getEventSubExtraRuns());
													break;
												}
												if ((this_event.getEventExtra() == null || this_event.getEventExtra().trim().isEmpty())
														&& (this_event.getEventHowOut() != null 
														&& (!this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.MANKAD)
														|| !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.TIMED_OUT)
														|| !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.RETIRED_OUT)
														|| !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.ABSENT_HURT)
														|| !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.RETIRED_HURT)))) // This is normal delivery
												{
													if(this_event.getDoNotIncrementBall().equalsIgnoreCase(CricketUtil.NO)) {
														if(this_event.getEventBallNo() <= Integer.valueOf(CricketUtil.DOT)) {
															inn.setTotalBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
															bc.setBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
															bc.setOvers(bc.getOvers() - 1);
															inn.setTotalOvers(inn.getTotalOvers() - 1);
															lastBallOfTheOver = true;
														} else {
															bc.setBalls(bc.getBalls() - 1);
															inn.setTotalBalls(inn.getTotalBalls() - 1);
														}
														if(session_match.getMatch().getDaysSessions() != null) {
															thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
																ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
															if(thisDaySession != null) {
																for(DaySession ds : bc.getBowlingSession()) {
																	if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																		&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																		ds.setTotalBalls(ds.getTotalBalls() - 1);
																	}
																}
															}
														}													
														for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
															if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
																inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() - 1);
															}
														}
														if(session_match.getMatch().getDaysSessions() != null) {
															for(DaySession ds : session_match.getMatch().getDaysSessions()) {
																if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
																	ds.setTotalBalls(ds.getTotalBalls() - 1);
																}
															}
														}
													}
												}
												break;
											}
										}
										bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
										if(this_event.getEventSubExtra() != null && (this_event.getEventSubExtra().equalsIgnoreCase(CricketUtil.BYE)
											|| this_event.getEventSubExtra().equalsIgnoreCase(CricketUtil.LEG_BYE))) {
											bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() - (this_event.getEventRuns() + this_event.getEventExtraRuns()));
										} else {
											bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() - (this_event.getEventRuns() + this_event.getEventExtraRuns() 
												+ this_event.getEventSubExtraRuns()));
										}
										inn.setTotalExtras(inn.getTotalWides() + inn.getTotalNoBalls() + inn.getTotalByes() + inn.getTotalLegByes() + inn.getTotalPenalties());
										inn.setTotalRuns(inn.getTotalRuns() - (this_event.getEventRuns() + this_event.getEventExtraRuns() + this_event.getEventSubExtraRuns()));
										if(session_match.getSetup().getSpecialMatchRules() != null 
											&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
											if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
												if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
													[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
													|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
												{
													if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
														if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
															inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
														} 
//														else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//															inn.setSpecialRuns("0");
//														} 
														else {
															inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
														}
													}
												}
											}
										}													
										inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() - 
											(this_event.getEventRuns() + this_event.getEventExtraRuns() + this_event.getEventSubExtraRuns()));
										if(session_match.getMatch().getDaysSessions() != null) {
											for(DaySession ds : session_match.getMatch().getDaysSessions()) {
												if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
													ds.setTotalRuns(ds.getTotalRuns() - (this_event.getEventRuns() + this_event.getEventExtraRuns() + this_event.getEventSubExtraRuns()));
												}
											}
										}
										if(session_match.getSetup().getSpeedFilePath() != null && !session_match.getSetup().getSpeedFilePath().isEmpty()) {
											if(bc.getSpeeds() != null && bc.getSpeeds().size() > 0) {
												bc.getSpeeds().removeIf(sp -> sp.getOverNumber() == bc.getOvers() 
													&& sp.getBallNumber() == bc.getBalls());
											}
										}
									}
								}
								int partnershipBalls = 0;
								if(this_event.getEventRuns() > 0 || this_event.getEventExtraRuns() > 0 || this_event.getEventSubExtraRuns() > 0) {
									partnershipBalls = 1;
								} else {
									switch (this_event.getEventExtra().toUpperCase()) {
									case CricketUtil.WIDE: case CricketUtil.NO_BALL:
										partnershipBalls = 1;
										break;
									default:
										//How out text found so increment part total balls
										if(!this_event.getEventHowOut().trim().isEmpty()) {
											switch (this_event.getEventHowOut().toUpperCase()) {
											case CricketUtil.MANKAD: case CricketUtil.RETIRED_HURT: case CricketUtil.RETIRED_OUT: 
											case CricketUtil.TIMED_OUT: case CricketUtil.ABSENT_HURT:
												break;
											default:
												partnershipBalls = 1;
												break;
											}
										} else {
											switch (this_event.getEventSubExtra().toUpperCase()) {
											case CricketUtil.WIDE: case CricketUtil.NO_BALL:
												partnershipBalls = 1;
												break;
											}
										}
										break;
									}
								}
								if(this_event.getDoNotIncrementBall().equalsIgnoreCase(CricketUtil.NO)) {
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() - partnershipBalls);
								}
								for(BattingCard bc:inn.getBattingCard()) {
									if(bc.getPlayerId() == this_event.getEventHowOutBatterNo()) {
										if(!this_event.getEventHowOut().trim().isEmpty()) { // How out text found
											bc.setStatus(CricketUtil.NOT_OUT);
											if(this_event.getEventHowOut() != null && !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.RETIRED_HURT)
												&& !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.CONCUSSED)) {
												inn.setTotalWickets(inn.getTotalWickets() - 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													for(DaySession ds : session_match.getMatch().getDaysSessions()) {
														if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
															ds.setTotalWickets(ds.getTotalWickets() - 1);
														}
													}
												}
												inn.getFallsOfWickets().remove(inn.getFallsOfWickets().size()-1);
											}
											//bc.setOnStrike(this_event.getEventOnStrike());
											bc.setHowOut("");
											bc.setHowOutBowlerId(0);
											bc.setHowOutFielderId(0);
											bc.setWasHowOutFielderSubstitute(this_event.getSubstitutionMade());
										}
									}
									if(bc.getPlayerId() == this_event.getEventBatterNo()) {
										bc.setRuns(bc.getRuns() - this_event.getEventRuns());
										bc.setOnStrike(this_event.getEventOnStrike());
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBattingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
													}
												}
											}
										}
										bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
										if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() - this_event.getEventRuns());
										} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() - this_event.getEventRuns());
										}
										if (this_event.getEventRuns() == Integer.valueOf(CricketUtil.FOUR) && this_event.getEventWasABoundary() != null 
												&& this_event.getEventWasABoundary().equalsIgnoreCase(CricketUtil.YES)) {
											bc.setFours(bc.getFours() - 1);
											inn.setTotalFours(inn.getTotalFours() - 1);
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalFours(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalFours() - 1);
											if(session_match.getMatch().getDaysSessions() != null) {
												for(DaySession ds : session_match.getMatch().getDaysSessions()) {
													if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
														ds.setTotalFours(ds.getTotalFours() - 1);
														for(DaySession bds : bc.getBattingSession()) {
															if(bds.getDayNumber() == ds.getDayNumber() 
																&& bds.getSessionNumber() == ds.getSessionNumber()) {
																bds.setTotalFours(bds.getTotalFours() - 1);
															}
														}
													}
												}
											}
										} else if (this_event.getEventRuns() == Integer.valueOf(CricketUtil.SIX) && this_event.getEventWasABoundary() != null 
												&& this_event.getEventWasABoundary().equalsIgnoreCase(CricketUtil.YES)) {
											bc.setSixes(bc.getSixes() - 1);
											inn.setTotalSixes(inn.getTotalSixes() - 1);
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalSixes(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalSixes() - 1);
											if(session_match.getMatch().getDaysSessions() != null) {
												for(DaySession ds : session_match.getMatch().getDaysSessions()) {
													if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
														ds.setTotalSixes(ds.getTotalSixes() - 1);
														for(DaySession bds : bc.getBattingSession()) {
															if(bds.getDayNumber() == ds.getDayNumber() 
																&& bds.getSessionNumber() == ds.getSessionNumber()) {
																bds.setTotalSixes(bds.getTotalSixes() - 1);
															}
														}
													}
												}
											}
										} else if (this_event.getEventRuns() == Integer.valueOf(CricketUtil.NINE) && this_event.getEventWasABoundary() != null 
												&& this_event.getEventWasABoundary().equalsIgnoreCase(CricketUtil.YES)) {
											bc.setNines(bc.getNines() - 1);
											inn.setTotalNines(inn.getTotalNines() - 1);
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalNines(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalNines() - 1);
										}
										if(this_event.getEventSubExtra() != null && this_event.getEventSubExtra().equalsIgnoreCase(CricketUtil.PENALTY)) {
										}if(this_event.getEventSubExtra() != null && this_event.getEventSubExtra().equalsIgnoreCase(CricketUtil.WIDE)) {
											if(lastBallOfTheOver == true) {
												switch (String.valueOf(this_event.getEventSubExtraRuns())) {
												case CricketUtil.DOT: case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: 
													bc.setOnStrike(CricketUtil.YES);
													break;
												}
											} else {
												switch (String.valueOf(this_event.getEventSubExtraRuns())) {
												case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
													bc.setOnStrike(CricketUtil.YES);
													break;
												}
											}
										} else {
											if(lastBallOfTheOver == true) {
												switch (String.valueOf(this_event.getEventRuns())) {
												case CricketUtil.DOT: case CricketUtil.TWO: 
													bc.setOnStrike(CricketUtil.YES);
													break;
												}
											} else {
												switch (String.valueOf(this_event.getEventRuns())) {
												case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
													bc.setOnStrike(CricketUtil.YES);
													break;
												}
											}
										}
										if(this_event.getEventExtra() != null) {
											switch (this_event.getEventExtra().trim().toUpperCase()) {
											case CricketUtil.WIDE: 
												break;
											case CricketUtil.NO_BALL:
												if(this_event.getDoNotIncrementBall().equalsIgnoreCase(CricketUtil.NO)) {
													bc.setBalls(bc.getBalls() - 1);
													if(session_match.getMatch().getDaysSessions() != null) {
														thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
															ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
														if(thisDaySession != null) {
															for(DaySession ds : bc.getBattingSession()) {
																if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																	&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																	ds.setTotalBalls(ds.getTotalBalls() - 1);
																}
															}
														}
													}
												}
												bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
												if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() - 1);
												} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() - 1);
												}
												break;
											default:
												switch (this_event.getEventSubExtra().trim().toUpperCase()) {
												case CricketUtil.WIDE: 
													break;
												case CricketUtil.NO_BALL:
													if(this_event.getDoNotIncrementBall().equalsIgnoreCase(CricketUtil.NO)) {
														bc.setBalls(bc.getBalls() - 1);
														if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
															inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
																inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() - 1);
														} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
															inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
																inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() - 1);
														}
													}
													bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
													break;
												default:
													if(this_event.getEventHowOut() != null 
														&& (!this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.RETIRED_HURT)
														|| !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.ABSENT_HURT)
														|| !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.TIMED_OUT)
														|| !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.MANKAD)
														|| !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.RETIRED_OUT))) {
														if(this_event.getDoNotIncrementBall().equalsIgnoreCase(CricketUtil.NO)) {
															bc.setBalls(bc.getBalls() - 1);
															if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
																inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
																	inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() - 1);
															} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
																inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
																	inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() - 1);
															}
														}
														bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
													}
													break;
												}
												break;
											}
										}
									} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
										bc.setOnStrike(CricketUtil.NO);
									}
//									} else if(bc.getPlayerId() == this_event.getEventOtherBatterNo()) {
//										if(this_event.getEventSubExtra() != null && this_event.getEventSubExtra().equalsIgnoreCase(CricketUtil.PENALTY)) {
//										}if(this_event.getEventSubExtra() != null && this_event.getEventSubExtra().equalsIgnoreCase(CricketUtil.WIDE)) {
//											if(lastBallOfTheOver == true) {
//												switch (String.valueOf(this_event.getEventSubExtraRuns())) {
//												case CricketUtil.DOT: case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: 
//													bc.setOnStrike(CricketUtil.NO);
//													break;
//												}
//											} else {
//												switch (String.valueOf(this_event.getEventSubExtraRuns())) {
//												case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
//													bc.setOnStrike(CricketUtil.NO);
//													break;
//												}
//											}
//										
//										} else {
//											if(lastBallOfTheOver == true) {
//												switch (String.valueOf(this_event.getEventRuns())) {
//												case CricketUtil.DOT: case CricketUtil.TWO:  
//													bc.setOnStrike(CricketUtil.NO);
//													break;
//												}
//											} else {
//												switch (String.valueOf(this_event.getEventRuns())) {
//												case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
//													bc.setOnStrike(CricketUtil.NO);
//													break;
//												}
//											}
//										}
//									}
									bc=CricketFunctions.processBattingcard(cricketService,bc);
								}
								if(this_event.getEventHowOut() != null && this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.CONCUSSED)) {
									if(this_event.getEventConcussionReplacePlayerId() > 0) {
										bc_itr = inn.getBattingCard().iterator();
										while(bc_itr.hasNext()) {
											if(bc_itr.next().getPlayerId() == this_event.getEventConcussionReplacePlayerId()) {
												bc_itr.remove();
											}
										}
									}
									break;
								}
							}
						}
						if(session_match.getMatch().getWagons() != null && session_match.getMatch().getWagons().size() > 0) {
							session_match.getMatch().getWagons().remove(session_match.getMatch().getWagons().size()-1);
						}
						if(session_match.getMatch().getShots() != null && session_match.getMatch().getShots().size() > 0) {
							session_match.getMatch().getShots().remove(session_match.getMatch().getShots().size()-1);
						}
						break;
					
					case CricketUtil.LOG_WICKET: 
					
						for(Inning inn:session_match.getMatch().getInning()) {
							if(inn.getInningNumber() == this_event.getEventInningNumber()) {
								if(inn.getBowlingCard() != null && inn.getBowlingCard().size() > 0) {
									for(BowlingCard bc:inn.getBowlingCard()) {
										if(bc.getPlayerId() == this_event.getEventBowlerNo()) 
										{
											switch (this_event.getEventHowOut().toUpperCase()) {
											case CricketUtil.CAUGHT_AND_BOWLED: case CricketUtil.CAUGHT: case CricketUtil.BOWLED: 
											case CricketUtil.STUMPED: case CricketUtil.LBW: case CricketUtil.HIT_WICKET:
												bc.setWickets(bc.getWickets() - 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBowlingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalWickets(ds.getTotalWickets() - 1);
															}
														}
													}
												}											
												if(this_event.getEventHowOut() != null && this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.CAUGHT_AND_BOWLED))
													bc.setCatchAsBowler(bc.getCatchAsBowler() - 1);
												for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
													if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
														inn.getSpells().get(i).setWickets(inn.getSpells().get(i).getWickets() - 1);
													}
												}
												break;
											}
											bc.setRuns(bc.getRuns() - this_event.getEventRuns());
											if(session_match.getMatch().getDaysSessions() != null) {
												thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
													ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
												if(thisDaySession != null) {
													for(DaySession ds : bc.getBowlingSession()) {
														if(ds.getDayNumber() == thisDaySession.getDayNumber() 
															&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
															ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
														}
													}
												}
											}
											switch (this_event.getEventHowOut().toUpperCase()) {
											case CricketUtil.ABSENT_HURT: case CricketUtil.RETIRED_HURT: case CricketUtil.MANKAD: 
											case CricketUtil.RETIRED_OUT: case CricketUtil.TIMED_OUT:
												break;
											default:
												switch (String.valueOf(this_event.getEventRuns())) {
												case CricketUtil.DOT: 
													bc.setDots(bc.getDots() - 1);
													break;
												}
												if(this_event.getEventBallNo() == Integer.valueOf(CricketUtil.DOT)) {
													inn.setTotalBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
													bc.setBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
													bc.setOvers(bc.getOvers() - 1);
													inn.setTotalOvers(inn.getTotalOvers() - 1);
													lastBallOfTheOver = true;
												} else {
													inn.setTotalBalls(inn.getTotalBalls() - 1);
													bc.setBalls(bc.getBalls() - 1);
												}
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBowlingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalBalls(ds.getTotalBalls() - 1);
															}
														}
													}
												}
												for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
													if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
														inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() - 1);
														inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() - this_event.getEventRuns());
													}
												}
												
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() - this_event.getEventRuns());
												
												if(session_match.getMatch().getDaysSessions() != null) {
													for(DaySession ds : session_match.getMatch().getDaysSessions()) {
														if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
															ds.setTotalBalls(ds.getTotalBalls() - 1);
														}
													}
												}
												break;
											}
											bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
											bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() - this_event.getEventRuns());
											inn.setTotalRuns(inn.getTotalRuns() - this_event.getEventRuns());
											if(session_match.getSetup().getSpecialMatchRules() != null 
												&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
												if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
													if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
														[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
														|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
													{
														if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
															if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
																inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
															} 
//															else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//																inn.setSpecialRuns("0");
//															} 
															else {
																inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
															}
														}
													}
												}
											}													
											if(session_match.getMatch().getDaysSessions() != null) {
												for(DaySession ds : session_match.getMatch().getDaysSessions()) {
													if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
														ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
													}
												}
											}
											inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),
												inn.getTotalBalls(),2, session_match));
											if(session_match.getSetup().getSpeedFilePath() != null && !session_match.getSetup().getSpeedFilePath().isEmpty()) {
												if(bc.getSpeeds() != null && bc.getSpeeds().size() > 0) {
													bc.getSpeeds().removeIf(sp -> sp.getOverNumber() == bc.getOvers() 
															&& sp.getBallNumber() == bc.getBalls());
												}
											}
										}
									}
								}
								for(BattingCard bc:inn.getBattingCard()) {
									if(bc.getPlayerId() == this_event.getEventHowOutBatterNo()) {
										if(!this_event.getEventHowOut().trim().isEmpty()) { // How out text found
											if (this_event.getEventHowOut() != null && !this_event.getEventHowOut().equalsIgnoreCase(
												CricketUtil.RETIRED_HURT)
												&& !this_event.getEventHowOut().equalsIgnoreCase(CricketUtil.CONCUSSED)) {
												inn.setTotalWickets(inn.getTotalWickets() - 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													for(DaySession ds : session_match.getMatch().getDaysSessions()) {
														if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
															ds.setTotalWickets(ds.getTotalWickets() - 1);
														}
													}
												}
												inn.getFallsOfWickets().remove(inn.getFallsOfWickets().size()-1);
											}
											bc.setStatus(CricketUtil.NOT_OUT);
											//bc.setOnStrike(this_event.getEventOnStrike());
											bc.setHowOut("");
											bc.setHowOutBowlerId(0);
											bc.setHowOutFielderId(0);
											bc.setWasHowOutFielderSubstitute(this_event.getSubstitutionMade());
										}
									}
									if(bc.getPlayerId() == this_event.getEventBatterNo()) {
										bc.setRuns(bc.getRuns() - this_event.getEventRuns());
										bc.setOnStrike(this_event.getEventOnStrike());
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBattingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
													}
												}
											}
										}
										if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() - this_event.getEventRuns());
										} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() - this_event.getEventRuns());
										}
										switch (this_event.getEventHowOut().toUpperCase()) {
										case CricketUtil.ABSENT_HURT: case CricketUtil.RETIRED_HURT: case CricketUtil.MANKAD: 
										case CricketUtil.RETIRED_OUT: case CricketUtil.TIMED_OUT:											
											break;
										default:
											bc.setBalls(bc.getBalls() - 1);
											if(session_match.getMatch().getDaysSessions() != null) {
												thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
													ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
												if(thisDaySession != null) {
													for(DaySession ds : bc.getBattingSession()) {
														if(ds.getDayNumber() == thisDaySession.getDayNumber() 
															&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
															ds.setTotalBalls(ds.getTotalBalls() - 1);
														}
													}
												}
											}
											if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() - 1);
											} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() - 1);
											}
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() - 1);												
											break;
										}
										bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
									} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
										bc.setOnStrike(CricketUtil.NO);
									}
									bc=CricketFunctions.processBattingcard(cricketService,bc);
								}
								switch (this_event.getEventHowOut().toUpperCase()) {
								case CricketUtil.CONCUSSED:
									if(this_event.getEventConcussionReplacePlayerId() > 0) {
										bc_itr = inn.getBattingCard().iterator();
										while(bc_itr.hasNext()) {
											if(bc_itr.next().getPlayerId() == this_event.getEventConcussionReplacePlayerId()) {
												bc_itr.remove();
											}
										}
									}
									break;
								}
							}
						}
						if(session_match.getMatch().getWagons() != null && session_match.getMatch().getWagons().size() > 0) {
							session_match.getMatch().getWagons().remove(session_match.getMatch().getWagons().size()-1);
						}
						if(session_match.getMatch().getShots() != null && session_match.getMatch().getShots().size() > 0) {
							session_match.getMatch().getShots().remove(session_match.getMatch().getShots().size()-1);
						}
						break;
					
					default:
						
						if(this_event.getEventType().equalsIgnoreCase(CricketUtil.DOT) || this_event.getEventType().equalsIgnoreCase(CricketUtil.ONE)
							|| this_event.getEventType().equalsIgnoreCase(CricketUtil.TWO) || this_event.getEventType().equalsIgnoreCase(CricketUtil.THREE)
							|| this_event.getEventType().equalsIgnoreCase(CricketUtil.FIVE) || this_event.getEventType().equalsIgnoreCase(CricketUtil.NO_BALL)
							|| this_event.getEventType().equalsIgnoreCase(CricketUtil.WIDE)) 
						{
							for(Inning inn:session_match.getMatch().getInning()) {
								if(inn.getInningNumber() == this_event.getEventInningNumber()) {
									for(BowlingCard bc : inn.getBowlingCard()) {
										
										if(bc.getPlayerId() == this_event.getEventBowlerNo()) 
										{
											switch (this_event.getEventType()) {
											case CricketUtil.DOT: case CricketUtil.ONE: case CricketUtil.TWO: case CricketUtil.THREE: case CricketUtil.FIVE:
												switch (String.valueOf(this_event.getEventRuns())) {
												case CricketUtil.DOT: 
													bc.setDots(bc.getDots() - 1);
												}
												inn.setTotalRuns(inn.getTotalRuns() - this_event.getEventRuns());
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() - this_event.getEventRuns());
												if(inn.getTotalBalls() <= Integer.valueOf(CricketUtil.DOT)) {
													inn.setTotalBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
													inn.setTotalOvers(inn.getTotalOvers() - 1);
													lastBallOfTheOver = true;
												} else {
													inn.setTotalBalls(inn.getTotalBalls() - 1);
												}
												bc.setRuns(bc.getRuns() - this_event.getEventRuns());
												bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() - this_event.getEventRuns());
												
												if(session_match.getSetup().getSpecialMatchRules() != null 
													&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
													if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
														if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
															[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
															|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
														{
															if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
																if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
																	inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
																} 
//																else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//																	inn.setSpecialRuns("0");
//																} 
																else {
																	inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
																}
															}
														}
													}
												}
												
												if(bc.getBalls() <= Integer.valueOf(CricketUtil.DOT)) {
													bc.setBalls(Integer.valueOf(session_match.getSetup().getBallsPerOver()) - 1);
													bc.setOvers(bc.getOvers() - 1);
												} else {
													bc.setBalls(bc.getBalls() - 1);
												}
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBowlingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
																ds.setTotalBalls(ds.getTotalBalls() - 1);
															}
														}
													}
												}
												for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
													if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
														inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() - 1);
														inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() - this_event.getEventRuns());
													}
												}
												bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
												if(session_match.getMatch().getDaysSessions() != null) {
													for(DaySession ds : session_match.getMatch().getDaysSessions()) {
														if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
															ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
															ds.setTotalBalls(ds.getTotalBalls() - 1);
														}
													}
												}
												inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),
													inn.getTotalBalls(), 2, session_match));
												break;
											
											default:
												
												if(this_event.getEventType().equalsIgnoreCase(CricketUtil.NO_BALL) || this_event.getEventType().equalsIgnoreCase(CricketUtil.WIDE)) {

													if(this_event.getEventExtra() != null) {
														switch (this_event.getEventExtra().toUpperCase()) {
														case CricketUtil.WIDE:
															bc.setRuns(bc.getRuns() - this_event.getEventExtraRuns());
															if(session_match.getMatch().getDaysSessions() != null) {
																thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
																	ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
																if(thisDaySession != null) {
																	for(DaySession ds : bc.getBowlingSession()) {
																		if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																			&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																			ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventExtraRuns());
																		}
																	}
																}
															}													
															bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() - this_event.getEventExtraRuns());
															bc.setWides(bc.getWides() - this_event.getEventExtraRuns());
															inn.setTotalWides(inn.getTotalWides() - this_event.getEventExtraRuns());
															inn.setTotalRuns(inn.getTotalRuns() - this_event.getEventExtraRuns());
															if(session_match.getSetup().getSpecialMatchRules() != null 
																&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
																if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
																	if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
																		[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
																		|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
																	{
																		if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {																	
																			if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
																				inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
																			} 
//																			else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//																				inn.setSpecialRuns("0");
//																			} 
																			else {
																				inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
																			}
																		}
																	}
																}
															}													
															break;
															
														case CricketUtil.NO_BALL:
															
															bc.setRuns(bc.getRuns() - Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
															if(session_match.getMatch().getDaysSessions() != null) {
																thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
																	ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
																if(thisDaySession != null) {
																	for(DaySession ds : bc.getBowlingSession()) {
																		if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																			&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																			ds.setTotalRuns(ds.getTotalRuns() - Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
																		}
																	}
																}
															}													
															bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() - Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
															bc.setNoBalls(bc.getNoBalls() - 1);
															inn.setTotalNoBalls(inn.getTotalNoBalls() - 1);
															inn.setTotalRuns(inn.getTotalRuns() - Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
															if(session_match.getSetup().getSpecialMatchRules() != null 
																&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
																if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
																	if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
																		[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
																		|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
																	{
																		if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
																			if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
																				inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
																			} 
//																			else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//																				inn.setSpecialRuns("0");
//																			} 
																			else {
																				inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
																			}
																		}
																	}
																}
															}													
															break;
														}
													}
												}
												bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(), bc.getOvers(),bc.getBalls(),2, session_match));
												inn.setTotalExtras(inn.getTotalWides() + inn.getTotalNoBalls() + inn.getTotalByes() 
													+ inn.getTotalLegByes() + inn.getTotalPenalties());
												
												if(session_match.getMatch().getDaysSessions() != null) {
													for(DaySession ds : session_match.getMatch().getDaysSessions()) {
														if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
															if(this_event.getEventType().equalsIgnoreCase(CricketUtil.NO_BALL)) {
																ds.setTotalRuns(ds.getTotalRuns() - Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
															} else if(this_event.getEventType().equalsIgnoreCase(CricketUtil.WIDE)) {
																ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventExtraRuns());
															} 
														}
													}
												}
												for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
													if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
														if(this_event.getEventType().equalsIgnoreCase(CricketUtil.NO_BALL)) {
															inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() - Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
														} else if(this_event.getEventType().equalsIgnoreCase(CricketUtil.WIDE)) {
															inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() - this_event.getEventExtraRuns());
														} 
													}
												}
												inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),
													inn.getTotalBalls(),2, session_match));
												if(this_event.getEventType().equalsIgnoreCase(CricketUtil.NO_BALL)) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() 
														- Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() - 1);										
												} else if(this_event.getEventType().contains(CricketUtil.WIDE)) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() 
														- this_event.getEventExtraRuns());
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() - 1);										
												} 
												break;
											}
											if(session_match.getSetup().getSpeedFilePath() != null 
												&& !session_match.getSetup().getSpeedFilePath().isEmpty()) {
												if(bc.getSpeeds() != null && bc.getSpeeds().size() > 0) {
													bc.getSpeeds().removeIf(sp -> sp.getOverNumber() == bc.getOvers() && sp.getBallNumber() == bc.getBalls());
												}
											}
										} 
									}
									for(BattingCard bc:inn.getBattingCard()) {
										if(this_event.getEventType().toUpperCase().equalsIgnoreCase(CricketUtil.WIDE)) {
											if(bc.getPlayerId() == this_event.getEventBatterNo()) {
												if(lastBallOfTheOver == true) {
													switch (String.valueOf(this_event.getEventExtraRuns())) {
													case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE: 
														bc.setOnStrike(CricketUtil.YES);
														break;
													}
												} else {
													switch (String.valueOf(this_event.getEventExtraRuns())) {
													case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX:
														bc.setOnStrike(CricketUtil.YES);
														break;
													}
												}
											}
											else if(bc.getPlayerId() == this_event.getEventOtherBatterNo()) {
												if(lastBallOfTheOver == true) {
													switch (String.valueOf(this_event.getEventExtraRuns())) {
													case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE: 
														bc.setOnStrike(CricketUtil.NO);
														break;
													}
												} else {
													switch (String.valueOf(this_event.getEventExtraRuns())) {
													case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX:
														bc.setOnStrike(CricketUtil.NO);
														break;
													}
												}
											}
											
										} else if(this_event.getEventType().toUpperCase().equalsIgnoreCase(CricketUtil.NO_BALL)) {
											
											if(bc.getPlayerId() == this_event.getEventBatterNo()) {
												bc.setBalls(bc.getBalls() - 1);
												bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBattingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() && ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalBalls(ds.getTotalBalls() - 1);
															}
														}
													}
												}
												if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() - 1);
												} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() - 1);
												}
											}
											
										} else {
											
											switch (String.valueOf(this_event.getEventRuns())) {
											case CricketUtil.DOT: case CricketUtil.ONE: case CricketUtil.TWO: case CricketUtil.THREE: case CricketUtil.FIVE: 
												if(bc.getPlayerId() == this_event.getEventBatterNo()) {
													bc.setRuns(bc.getRuns() - this_event.getEventRuns());
													bc.setBalls(bc.getBalls() - 1);
													if(session_match.getMatch().getDaysSessions() != null) {
														thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
															ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
														if(thisDaySession != null) {
															for(DaySession ds : bc.getBattingSession()) {
																if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																	&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																	ds.setTotalBalls(ds.getTotalBalls() - 1);
																	ds.setTotalRuns(ds.getTotalRuns() - this_event.getEventRuns());
																}
															}
														}
													}
													bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
													if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
														inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
															inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() - this_event.getEventRuns());
														inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
															inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() - 1);
													} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
														inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
															inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() - this_event.getEventRuns());
														inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
															inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() - 1);
													}
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() - 1);										
													if(lastBallOfTheOver == true) {
														switch (String.valueOf(this_event.getEventRuns())) {
														case CricketUtil.DOT: case CricketUtil.TWO: 
															bc.setOnStrike(CricketUtil.YES);
															break;
														}
													} else {
														switch (String.valueOf(this_event.getEventRuns())) {
														case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
															bc.setOnStrike(CricketUtil.YES);
															break;
														}
													}
												} else if(bc.getPlayerId() == this_event.getEventOtherBatterNo()) {
													if(lastBallOfTheOver == true) {
														switch (String.valueOf(this_event.getEventRuns())) {
														case CricketUtil.DOT: case CricketUtil.TWO:  
															bc.setOnStrike(CricketUtil.NO);
															break;
														}
													} else {
														switch (String.valueOf(this_event.getEventRuns())) {
														case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
															bc.setOnStrike(CricketUtil.NO);
															break;
														}
													}
												}
												break;
											}
										}
									}
								}
							}
							switch (this_event.getEventType()) {
							case CricketUtil.DOT:
								if(session_match.getMatch().getShots() != null && session_match.getMatch().getShots().size() > 0) {
									session_match.getMatch().getShots().remove(session_match.getMatch().getShots().size()-1);
								}
								break;
							case CricketUtil.ONE: case CricketUtil.TWO: case CricketUtil.THREE: case CricketUtil.FIVE:
								if(session_match.getMatch().getWagons() != null && session_match.getMatch().getWagons().size() > 0) {
									session_match.getMatch().getWagons().remove(session_match.getMatch().getWagons().size()-1);
								}
								if(session_match.getMatch().getShots() != null && session_match.getMatch().getShots().size() > 0) {
									session_match.getMatch().getShots().remove(session_match.getMatch().getShots().size()-1);
								}
								break;
							}
						}
						break;
					}
					session_match.getEventFile().getEvents().remove(this_event);
				}
			}
			for(Inning inn : session_match.getMatch().getInning()) {
				if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
					if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
						session_match.getMatch().setMatchStatus(CricketFunctions.GenerateMatchSummaryStatus(
							inn.getInningNumber(), session_match, "", "", "", false).getTargetOrResult());
					}
				}
			}
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.SETUP + "," + CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");
			return objectMapper.writeValueAsString(session_match);

		case CricketUtil.LOG_EVENT:
			return processLogEvent(whatToProcess, timeStatsToProcess, valueToProcess, challengeEvent);
			
		case CricketUtil.LOG_ANY_BALL: 
			return processLogAnyBallEvent(whatToProcess, timeStatsToProcess, valueToProcess, challengeEvent);
			
		default:
			return processVariousMatchDataStats(whatToProcess, timeStatsToProcess, valueToProcess, challengeEvent);
		}
	}
	
	public String processLogEvent(String whatToProcess,String timeStatsToProcess,String valueToProcess, Event challengeEvent) throws Exception
	{
		boolean lastBallOfTheOver = false, onStrikeBatsmanFound = false; 	
		int which_bowler = 0, batter_position = 0, new_bat_last_pos = 0;
		Event this_event = new Event(whatToProcess, valueToProcess);
		DaySession thisDaySession = new DaySession();
		List<DaySession> thisBatSess = new ArrayList<DaySession>();
		BattingCard this_bc = new BattingCard();

		switch (whatToProcess.toUpperCase()) {
		case CricketUtil.LOG_EVENT:
			
			if(!valueToProcess.trim().isEmpty()) {

				if(session_match.getEventFile() == null)
					session_match.setEventFile(new EventFile());
				if(session_match.getEventFile().getEvents() == null)
					session_match.getEventFile().setEvents(new ArrayList<Event>());

				this_event.setEventNumber(session_match.getEventFile().getEvents().size() + 1);
				
				for(Inning inn:session_match.getMatch().getInning()) { 
				
					if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {

						this_event.setEventInningNumber(inn.getInningNumber());
						if (valueToProcess.toUpperCase().contains(CricketUtil.LEG_BYE) || valueToProcess.toUpperCase().contains(CricketUtil.BYE)) {

							for(BowlingCard bc:inn.getBowlingCard()) {
								if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.CURRENT + CricketUtil.BOWLER)) {
									this_event.setEventBowlerNo(bc.getPlayerId());
									if(session_speed != null && session_speed.getSpeedValue() != null && Double.valueOf(session_speed.getSpeedValue()) > 0) {
										this_event.setEventSpeed(session_speed.getSpeedValue());
									}
									if(inn.getTotalBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver())) {
										inn.setTotalBalls(Integer.valueOf(CricketUtil.DOT));
										inn.setTotalOvers(inn.getTotalOvers() + 1);
										lastBallOfTheOver = true;
									} else {
										inn.setTotalBalls(inn.getTotalBalls() + 1);
									}
									if(bc.getBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver())) {
										bc.setBalls(Integer.valueOf(CricketUtil.DOT));
										bc.setOvers(bc.getOvers() + 1);
									} else {
										bc.setBalls(bc.getBalls() + 1);
									}
									if(session_match.getMatch().getDaysSessions() != null) {
										thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
											ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
										if(thisDaySession != null) {
											for(DaySession ds : bc.getBowlingSession()) {
												if(ds.getDayNumber() == thisDaySession.getDayNumber() 
													&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
													ds.setTotalBalls(ds.getTotalBalls() + 1);
												}
											}
										}
									}									
									if(session_match.getSetup().getSpeedFilePath() != null && !session_match.getSetup().getSpeedFilePath().isEmpty()
											&& session_match.getMatch().getCurrent_speed() != null && !session_match.getMatch().getCurrent_speed().isEmpty()) {
										if(bc.getSpeeds() == null) {
											bc.setSpeeds(new ArrayList<Speed>());
										}
										bc.getSpeeds().add(new Speed(bc.getSpeeds().size() + 1, session_match.getMatch().getCurrent_speed(), 
											"", bc.getOvers(), bc.getBalls(), inn.getTotalOvers(), inn.getTotalBalls(), inn.getInningNumber()));
									}
									for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
										if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
											inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() + 1);
										}
									}
									bc.setDots(bc.getDots() + 1);
									bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
									inn.setTotalRuns(inn.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[1]));
									if(session_match.getSetup().getSpecialMatchRules() != null 
										&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
										if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
											if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
												[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
												|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
											{
												if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
													if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
														inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
													} 
//													else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//														inn.setSpecialRuns("0");
//													} 
													else {
														inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
													}
												}
											}
										}
									}													
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() 
										+ Integer.valueOf(valueToProcess.split(",")[1]));
									this_event.setEventRuns(Integer.valueOf(valueToProcess.split(",")[1]));
									if(session_match.getMatch().getDaysSessions() != null) {
										for(DaySession ds : session_match.getMatch().getDaysSessions()) {
											if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
												ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[1]));
												ds.setTotalBalls(ds.getTotalBalls() + 1);
											}
										}
									}
									inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));
									this_event.setEventOverNo(inn.getTotalOvers());
									this_event.setEventBallNo(inn.getTotalBalls());
									this_event.setEventExtraRuns(Integer.valueOf(valueToProcess.split(",")[1]));
									if(valueToProcess.toUpperCase().contains(CricketUtil.LEG_BYE)) {
										inn.setTotalLegByes(inn.getTotalLegByes() + Integer.valueOf(valueToProcess.split(",")[1]));
										this_event.setEventExtra(CricketUtil.LEG_BYE);
										this_event.setEventType(CricketUtil.LEG_BYE);
									} else if(valueToProcess.toUpperCase().contains(CricketUtil.BYE)) {
										inn.setTotalByes(inn.getTotalByes() + Integer.valueOf(valueToProcess.split(",")[1]));
										this_event.setEventExtra(CricketUtil.BYE);
										this_event.setEventType(CricketUtil.BYE);
									}
									inn.setTotalExtras(inn.getTotalWides() + inn.getTotalNoBalls() + inn.getTotalByes() + inn.getTotalLegByes() + inn.getTotalPenalties());
								} else if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.OTHER + CricketUtil.BOWLER)) {
									this_event.setEventOtherBowlerNo(bc.getPlayerId());
								}
							}
							
							for(BattingCard bc:inn.getBattingCard()) {
								if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)) {
									if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
										this_event.setEventBatterNo(bc.getPlayerId());
										bc.setBalls(bc.getBalls() + 1);
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBattingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalBalls(ds.getTotalBalls() + 1);
													}
												}
											}
										}										
										bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
										if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() + 1);
										} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() + 1);
										}
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);										
										if(lastBallOfTheOver == true) {
											switch (valueToProcess.split(",")[1].toUpperCase()) {
											case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: case CricketUtil.NINE:
												bc.setOnStrike(CricketUtil.NO);
												break;
											}
										} else {
											switch (valueToProcess.split(",")[1].toUpperCase()) {
											case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
												bc.setOnStrike(CricketUtil.NO);
												break;
											}
										}
									} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.NO)) {
										this_event.setEventOtherBatterNo(bc.getPlayerId());
										if(lastBallOfTheOver == true) {
											switch (valueToProcess.split(",")[1].toUpperCase()) {
											case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: case CricketUtil.NINE:
												bc.setOnStrike(CricketUtil.YES);
												break;
											}
										} else {
											switch (valueToProcess.split(",")[1].toUpperCase()) {
											case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
												bc.setOnStrike(CricketUtil.YES);
												break;
											}
										}
									} 
								} 
							}

						} else if(valueToProcess.toUpperCase().contains(CricketUtil.NEW_BATSMAN)) {
							
							Collections.sort(inn.getBattingCard());
							this_event.setEventInningNumber(inn.getInningNumber());
							this_event.setEventType(CricketUtil.NEW_BATSMAN);
							this_event.setEventBatterNo(Integer.valueOf(valueToProcess.split(",")[1]));
							
							batter_position = (int) inn.getBattingCard().stream().filter(bc ->
								bc.getBatsmanInningStarted() != null && bc.getBatsmanInningStarted().equalsIgnoreCase(
								CricketUtil.YES)).count() + 1;
							
							onStrikeBatsmanFound = false;
							if (inn.getBattingCard().stream().filter(bc -> bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)
								&& bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)).count() > 0)
							{
								onStrikeBatsmanFound = true;
							}

							if(inn.getBattingCard().stream().filter(bc -> bc.getPlayerId() // Add impact player to batting card
								== Integer.valueOf(valueToProcess.split(",")[1])).findFirst().orElse(null) == null) {

								this_bc = new BattingCard(Integer.valueOf(valueToProcess.split(",")[1]), 
									batter_position, CricketUtil.NOT_OUT);
								this_bc.setBatsmanInningStarted(CricketUtil.YES);
								this_bc.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
								
								if(onStrikeBatsmanFound == false) {
									this_bc.setOnStrike(CricketUtil.YES);
								} else {
									this_bc.setOnStrike(CricketUtil.NO);
								}
								if(session_match.getMatch().getDaysSessions() != null) {
									thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
										ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
									if(thisDaySession != null) {
										thisBatSess.add(new DaySession(thisDaySession.getDayNumber(), thisDaySession.getSessionNumber()));
										this_bc.setBattingSession(thisBatSess);
									}
								}
								new_bat_last_pos = inn.getBattingCard().size() + 1;
								
								inn.getBattingCard().add(batter_position - 1, CricketFunctions.processBattingcard(cricketService, this_bc));
								
								if(inn.getBattingTeamId() == session_match.getSetup().getHomeTeamId()) {
									
									this_event.setEventBattingCard(new BattingCard(session_match.getSetup()
										.getHomeSubstitutes().stream().filter(sub -> sub.getPlayerId() 
										== Integer.valueOf(valueToProcess.split(",")[1])).findAny().orElse(null)));
									
									session_match.getSetup().getHomeSubstitutes().removeIf(sub -> sub.getPlayerId() 
										== Integer.valueOf(valueToProcess.split(",")[1]));
								
								}else if(inn.getBattingTeamId() == session_match.getSetup().getAwayTeamId()) {

									this_event.setEventBattingCard(new BattingCard(session_match.getSetup()
										.getAwaySubstitutes().stream().filter(sub -> sub.getPlayerId() 
										== Integer.valueOf(valueToProcess.split(",")[1])).findAny().orElse(null)));
									
									session_match.getSetup().getAwaySubstitutes().removeIf(sub -> sub.getPlayerId() 
										== Integer.valueOf(valueToProcess.split(",")[1]));
								
								}

								this_bc = inn.getBattingCard().stream().filter(bc -> bc.getPlayerId() != Integer.valueOf(valueToProcess.split(",")[1]) 
									&& bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)).findFirst().orElse(null);
								
								if(this_bc != null) {
									this_event.setEventOtherBatterNo(this_bc.getPlayerId());
								} else {
									this_event.setEventOtherBatterNo(0);
								}
								this_event.setSubstitutionMade(CricketUtil.YES);
								
							} else {
								
								for(BattingCard bc:inn.getBattingCard()) {
									
									if(bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[1])) {

										bc.setStatus(CricketUtil.NOT_OUT);
										new_bat_last_pos = bc.getBatterPosition();
										bc.setBatsmanInningStarted(CricketUtil.YES);
										bc.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
										
										if(onStrikeBatsmanFound == false) {
											bc.setOnStrike(CricketUtil.YES);
										} else {
											bc.setOnStrike(CricketUtil.NO);
										}
										if(bc.getHowOut() != null && (bc.getHowOut().equalsIgnoreCase(CricketUtil.RETIRED_HURT))) {
											this_event.setEventHowOut(bc.getHowOut());
											bc.setHowOut("");
										} else {
											bc.setBatterPosition(batter_position);
										}
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												if(bc.getBattingSession() == null) {
													bc.setBattingSession(new ArrayList<DaySession>());
												}
												which_bowler = 0;
												for (DaySession batSess : bc.getBattingSession()) {
													if(thisDaySession.getDayNumber() == batSess.getDayNumber() 
														&& thisDaySession.getSessionNumber() == batSess.getSessionNumber()) {
														which_bowler = bc.getPlayerId();
													}
												}
												if(which_bowler <= 0) {
													bc.getBattingSession().add(new DaySession(thisDaySession.getDayNumber(), thisDaySession.getSessionNumber()));
												}
											}
										}
										
									} else if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)) {
									
										this_event.setEventOtherBatterNo(bc.getPlayerId());
									
									}
								}
							}

							if(this_event.getEventOtherBatterNo() > 0 && this_event.getEventBatterNo() > 0) {

								if(inn.getPartnerships() == null || inn.getPartnerships().size() <= 0) {
									inn.setPartnerships(new ArrayList<Partnership>());
								}
								
								Partnership part = new Partnership();
								if(inn.getPartnerships().size() <= 0) {
									part = new Partnership(inn.getPartnerships().size() + 1,
										this_event.getEventOtherBatterNo(),this_event.getEventBatterNo());
									part.setFirstPlayer(cricketService.getPlayer(CricketUtil.PLAYER, 
										String.valueOf(this_event.getEventOtherBatterNo())));
									part.setSecondPlayer(cricketService.getPlayer(CricketUtil.PLAYER, 
										String.valueOf(this_event.getEventBatterNo())));
								} else {
									if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == this_event.getEventOtherBatterNo())
									{
										part = new Partnership(inn.getPartnerships().size() + 1,
											this_event.getEventOtherBatterNo(),this_event.getEventBatterNo());
										part.setFirstPlayer(cricketService.getPlayer(CricketUtil.PLAYER, 
											String.valueOf(this_event.getEventOtherBatterNo())));
										part.setSecondPlayer(cricketService.getPlayer(CricketUtil.PLAYER, 
											String.valueOf(this_event.getEventBatterNo())));
										
									} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == this_event.getEventOtherBatterNo())
									{
										part = new Partnership(inn.getPartnerships().size() + 1,
											this_event.getEventBatterNo(),this_event.getEventOtherBatterNo());
										part.setFirstPlayer(cricketService.getPlayer(CricketUtil.PLAYER, 
											String.valueOf(this_event.getEventBatterNo())));
										part.setSecondPlayer(cricketService.getPlayer(CricketUtil.PLAYER, 
											String.valueOf(this_event.getEventOtherBatterNo())));
									}
								}
								
								inn.getPartnerships().add(part);
								
							}

							this_event.setEventBatterPosition(batter_position);
							this_event.setEventBatterPreviousPosition(new_bat_last_pos);
							if(onStrikeBatsmanFound == false) {
								this_event.setEventOnStrike(CricketUtil.YES);
							} else {
								this_event.setEventOnStrike(CricketUtil.NO);
							}
							
							Collections.sort(inn.getBattingCard());

							new_bat_last_pos = (int) inn.getBattingCard().stream().filter(bc ->
								bc.getBatsmanInningStarted() != null && bc.getBatsmanInningStarted().equalsIgnoreCase(
								CricketUtil.YES)).count() + 1;
							for(BattingCard bc:inn.getBattingCard()) {
								if((bc.getBatsmanInningStarted() == null) || (bc.getBatsmanInningStarted() != null 
										&& bc.getBatsmanInningStarted().equalsIgnoreCase(CricketUtil.NO))) {
									new_bat_last_pos = new_bat_last_pos + 1;
									bc.setBatterPosition(new_bat_last_pos);
								}
							}
							
						} else if ((valueToProcess.toUpperCase().contains(CricketUtil.FOUR) || valueToProcess.toUpperCase().contains(CricketUtil.SIX)
								|| valueToProcess.toUpperCase().contains(CricketUtil.NINE)) && (valueToProcess.toUpperCase().contains(CricketUtil.BOUNDARY)
								|| valueToProcess.toUpperCase().contains(CricketUtil.RUNS))) {

							for(BowlingCard bc:inn.getBowlingCard()) {
								if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.CURRENT + CricketUtil.BOWLER)) {
									this_event.setEventBowlerNo(bc.getPlayerId());
									if(session_speed != null && session_speed.getSpeedValue() != null && Double.valueOf(session_speed.getSpeedValue()) > 0) {
										this_event.setEventSpeed(session_speed.getSpeedValue());
									}
									if(inn.getTotalBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver())) {
										inn.setTotalBalls(Integer.valueOf(CricketUtil.DOT));
										inn.setTotalOvers(inn.getTotalOvers() + 1);
										lastBallOfTheOver = true;
									} else {
										inn.setTotalBalls(inn.getTotalBalls() + 1);
									}
									if(bc.getBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver())) {
										bc.setBalls(Integer.valueOf(CricketUtil.DOT));
										bc.setOvers(bc.getOvers() + 1);
										which_bowler = bc.getPlayerId();
									} else {
										bc.setBalls(bc.getBalls() + 1);
									}
									if(session_match.getMatch().getDaysSessions() != null) {
										thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
											ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
										if(thisDaySession != null) {
											for(DaySession ds : bc.getBowlingSession()) {
												if(ds.getDayNumber() == thisDaySession.getDayNumber() 
													&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
													ds.setTotalBalls(ds.getTotalBalls() + 1);
												}
											}
										}
									}									
									if(session_match.getSetup().getSpeedFilePath() != null && !session_match.getSetup().getSpeedFilePath().isEmpty()
											&& session_match.getMatch().getCurrent_speed() != null && !session_match.getMatch().getCurrent_speed().isEmpty()) {
										if(bc.getSpeeds() == null) {
											bc.setSpeeds(new ArrayList<Speed>());
										}
										bc.getSpeeds().add(new Speed(bc.getSpeeds().size() + 1, session_match.getMatch().getCurrent_speed(), 
												"", bc.getOvers(), bc.getBalls(), inn.getTotalOvers(), inn.getTotalBalls(), inn.getInningNumber()));
									}
									if(valueToProcess.toUpperCase().contains(CricketUtil.FOUR)) {
										bc.setRuns(bc.getRuns() + Integer.valueOf(CricketUtil.FOUR));
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(CricketUtil.FOUR));
														ds.setTotalFours(ds.getTotalFours() + 1);
														ds.setTotalBalls(ds.getTotalBalls() + 1);
													}
												}
											}
										}									
										bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + Integer.valueOf(CricketUtil.FOUR));
										inn.setTotalRuns(inn.getTotalRuns() + Integer.valueOf(CricketUtil.FOUR));
										if(session_match.getMatch().getDaysSessions() != null) {
											for(DaySession ds : session_match.getMatch().getDaysSessions()) {
												if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
													ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(CricketUtil.FOUR));
													ds.setTotalFours(ds.getTotalFours() + 1);
													ds.setTotalBalls(ds.getTotalBalls() + 1);
												}
											}
										}
										this_event.setEventType(CricketUtil.FOUR);
										this_event.setEventRuns(Integer.valueOf(CricketUtil.FOUR));
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() + Integer.valueOf(CricketUtil.FOUR));
									} else if(valueToProcess.toUpperCase().contains(CricketUtil.SIX)) {
										bc.setRuns(bc.getRuns() + Integer.valueOf(CricketUtil.SIX));
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(CricketUtil.SIX));
														ds.setTotalSixes(ds.getTotalSixes() + 1);
														ds.setTotalBalls(ds.getTotalBalls() + 1);
													}
												}
											}
										}									
										bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + Integer.valueOf(CricketUtil.SIX));
										inn.setTotalRuns(inn.getTotalRuns() + Integer.valueOf(CricketUtil.SIX));
//										if(session_match.getSetup().getSpecialMatchRules() != null 
//											&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
//											if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
//												if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
//													[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
//													|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
//												{
//													if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
//														if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) > 0) {
//															inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
//														} 
//														else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//															inn.setSpecialRuns("0");
//														} 
//														else {
//															inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
//														}
//													}
//												}
//											}
//										}													
										if(session_match.getMatch().getDaysSessions() != null) {
											for(DaySession ds : session_match.getMatch().getDaysSessions()) {
												if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
													ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(CricketUtil.SIX));
													ds.setTotalSixes(ds.getTotalSixes() + 1);
													ds.setTotalBalls(ds.getTotalBalls() + 1);
												}
											}
										}
										this_event.setEventType(CricketUtil.SIX);
										this_event.setEventRuns(Integer.valueOf(CricketUtil.SIX));
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() + Integer.valueOf(CricketUtil.SIX));
									} else if(valueToProcess.toUpperCase().contains(CricketUtil.NINE)) {
										bc.setRuns(bc.getRuns() + Integer.valueOf(CricketUtil.NINE));
										bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + Integer.valueOf(CricketUtil.NINE));
										inn.setTotalRuns(inn.getTotalRuns() + Integer.valueOf(CricketUtil.NINE));
//										if(session_match.getSetup().getSpecialMatchRules() != null 
//											&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
//											if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
//												if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
//													[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
//													|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
//												{
//													if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
//														if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) > 0) {
//															inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
//														} 
//														else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//															inn.setSpecialRuns("0");
//														} 
//														else {
//															inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
//														}
//													}
//												}
//											}
//										}													
										this_event.setEventType(CricketUtil.NINE);
										this_event.setEventRuns(Integer.valueOf(CricketUtil.NINE));
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() + Integer.valueOf(CricketUtil.NINE));
									}
									if(session_match.getSetup().getSpecialMatchRules() != null 
										&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
										if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
											if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
												[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
												|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
											{
												if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
													if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
														inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
													} 
//													else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//														inn.setSpecialRuns("0");
//													} 
													else {
														inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
													}
												}
											}
										}
									}													
									for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
										if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
											inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() + 1);
											if(valueToProcess.toUpperCase().contains(CricketUtil.FOUR)) {
												inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + Integer.valueOf(CricketUtil.FOUR));
											} else if(valueToProcess.toUpperCase().contains(CricketUtil.SIX)) {
												inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + Integer.valueOf(CricketUtil.SIX));
											} else if(valueToProcess.toUpperCase().contains(CricketUtil.NINE)) {
												inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + Integer.valueOf(CricketUtil.NINE));
											}
										}
									}
									bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
									inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));
									this_event.setEventOverNo(inn.getTotalOvers());
									this_event.setEventBallNo(inn.getTotalBalls());
									
								} else if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.OTHER + CricketUtil.BOWLER)) {

									this_event.setEventOtherBowlerNo(bc.getPlayerId());
									
								}
							}
							
							for(BattingCard bc:inn.getBattingCard()) {
								if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)) {
									if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
										this_event.setEventBatterNo(bc.getPlayerId());
										if(valueToProcess.toUpperCase().contains(CricketUtil.FOUR)) {
											bc.setRuns(bc.getRuns() + Integer.valueOf(CricketUtil.FOUR));
											if(session_match.getMatch().getDaysSessions() != null) {
												thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
													ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
												if(thisDaySession != null) {
													for(DaySession ds : bc.getBattingSession()) {
														if(ds.getDayNumber() == thisDaySession.getDayNumber() 
															&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
															ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(CricketUtil.FOUR));
														}
													}
												}
											}											
											if(valueToProcess.toUpperCase().contains(CricketUtil.BOUNDARY)) {
												bc.setFours(bc.getFours() + 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBattingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalFours(ds.getTotalFours() + 1);
															}
														}
													}
												}											
												this_event.setEventWasABoundary(CricketUtil.YES);
												inn.setTotalFours(inn.getTotalFours() + 1);
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalFours(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalFours() + 1);
											}
											if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() + Integer.valueOf(CricketUtil.FOUR));
											} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() + Integer.valueOf(CricketUtil.FOUR));
											}
										} else if(valueToProcess.toUpperCase().contains(CricketUtil.SIX)) {
											bc.setRuns(bc.getRuns() + Integer.valueOf(CricketUtil.SIX));
											if(session_match.getMatch().getDaysSessions() != null) {
												thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
													ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
												if(thisDaySession != null) {
													for(DaySession ds : bc.getBattingSession()) {
														if(ds.getDayNumber() == thisDaySession.getDayNumber() 
															&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
															ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(CricketUtil.SIX));
														}
													}
												}
											}											
											if(valueToProcess.toUpperCase().contains(CricketUtil.BOUNDARY)) {
												bc.setSixes(bc.getSixes() + 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBattingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalSixes(ds.getTotalSixes() + 1);
															}
														}
													}
												}											
												this_event.setEventWasABoundary(CricketUtil.YES);
												inn.setTotalSixes(inn.getTotalSixes() + 1);
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalSixes(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalSixes() + 1);
											}
											if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() + Integer.valueOf(CricketUtil.SIX));
											} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() + Integer.valueOf(CricketUtil.SIX));
											}
										} else if(valueToProcess.toUpperCase().contains(CricketUtil.NINE)) {
											bc.setRuns(bc.getRuns() + Integer.valueOf(CricketUtil.NINE));
											if(valueToProcess.toUpperCase().contains(CricketUtil.BOUNDARY)) {
												bc.setNines(bc.getNines() + 1);
												this_event.setEventWasABoundary(CricketUtil.YES);
												inn.setTotalNines(inn.getTotalNines() + 1);
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalNines(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalNines() + 1);
											}
											if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() 
													+ Integer.valueOf(CricketUtil.NINE));
											} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() 
													+ Integer.valueOf(CricketUtil.NINE));
											}
										}
										bc.setBalls(bc.getBalls() + 1);
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBattingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalBalls(ds.getTotalBalls() + 1);
													}
												}
											}
										}											
										bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
										if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() + 1);
										} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() + 1);
										}
										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);										
										if(lastBallOfTheOver == true) {
											bc.setOnStrike(CricketUtil.NO);
										}
									} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.NO)) {
										this_event.setEventOtherBatterNo(bc.getPlayerId());
										if(lastBallOfTheOver == true) {
											bc.setOnStrike(CricketUtil.YES);
										}
									} 
								 } 
							} 
							
						} else if(valueToProcess.equalsIgnoreCase(CricketUtil.SWAP_BATSMAN)) {
							
							this_event.setEventType(CricketUtil.SWAP_BATSMAN);
							this_event.setEventInningNumber(inn.getInningNumber());
							
							onStrikeBatsmanFound = false;
							if((inn.getBattingCard().stream().filter(bc -> bc.getStatus() != null 
									&& bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)
									&& bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)
									).count() > 1) || (inn.getBattingCard().stream().filter(bc -> bc.getStatus() != null 
									&& bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)
									&& bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.NO)
									).count() > 1)) {
								for(BattingCard bc:inn.getBattingCard()) {
									if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)) {
										if(onStrikeBatsmanFound == false) {
											this_event.setEventBatterNo(bc.getPlayerId());
											bc.setOnStrike(CricketUtil.YES);
											onStrikeBatsmanFound = true;
										} else {
											this_event.setEventOtherBatterNo(bc.getPlayerId());
											bc.setOnStrike(CricketUtil.NO);
										}
									}
								}
							} else {
								for(BattingCard bc:inn.getBattingCard()) {
									if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)) {
										if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
											this_event.setEventBatterNo(bc.getPlayerId());
											bc.setOnStrike(CricketUtil.NO);
										} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.NO)) {
											this_event.setEventOtherBatterNo(bc.getPlayerId());
											bc.setOnStrike(CricketUtil.YES);
										}
									}
								}
							}
							
						} else if(valueToProcess.equalsIgnoreCase(CricketUtil.END_OVER)) {

							this_event.setEventType(valueToProcess);
							
							for(BowlingCard bc:inn.getBowlingCard()) {
								if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.CURRENT + CricketUtil.BOWLER)) {
									if(bc.getBalls() <= 0) {
										if(bc.getTotalRunsThisOver() <= 0) {
											bc.setMaidens(bc.getMaidens() + 1);
											if(session_match.getMatch().getDaysSessions() != null) {
												thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
													ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
												if(thisDaySession != null) {
													for(DaySession ds : bc.getBowlingSession()) {
														if(ds.getDayNumber() == thisDaySession.getDayNumber() 
															&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
															ds.setTotalMaidens(ds.getTotalMaidens() + 1);
														}
													}
												}
											}									
										}
										this_event.setEventTotalRunsInAnOver(bc.getTotalRunsThisOver());
									}
									bc.setStatus(CricketUtil.LAST + CricketUtil.BOWLER);
									this_event.setEventBowlerNo(bc.getPlayerId());
									this_event.setEventBowlingEnd(bc.getBowling_end());
									this_event.setEventOverNo(inn.getTotalOvers());
									this_event.setEventBallNo(inn.getTotalBalls());
								}
							}
							
						} else {
							if(valueToProcess.toUpperCase().contains(CricketUtil.WIDE) 
								|| valueToProcess.toUpperCase().equalsIgnoreCase(CricketUtil.DOT)
								|| valueToProcess.toUpperCase().equalsIgnoreCase(CricketUtil.ONE)
								|| valueToProcess.toUpperCase().equalsIgnoreCase(CricketUtil.TWO)
								|| valueToProcess.toUpperCase().equalsIgnoreCase(CricketUtil.THREE)
								|| valueToProcess.toUpperCase().equalsIgnoreCase(CricketUtil.FIVE)
								|| valueToProcess.toUpperCase().equalsIgnoreCase(CricketUtil.NO_BALL)) 
							{ 
								if(valueToProcess.toUpperCase().contains(CricketUtil.WIDE)) {
									if(valueToProcess.contains(",")) {
										this_event.setEventType(CricketUtil.WIDE);
										this_event.setEventExtraRuns(Integer.valueOf(valueToProcess.split(",")[1]));
									}
								} else {
									this_event.setEventType(valueToProcess.toUpperCase());
								}
								for(BowlingCard bc:inn.getBowlingCard()) {
									if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.CURRENT + CricketUtil.BOWLER)) {
										this_event.setEventBowlerNo(bc.getPlayerId());
										if(session_speed != null && session_speed.getSpeedValue() != null && Double.valueOf(session_speed.getSpeedValue()) > 0) {
											this_event.setEventSpeed(session_speed.getSpeedValue());
										}
										switch (valueToProcess.toUpperCase()) {
										case CricketUtil.DOT: case CricketUtil.ONE: case CricketUtil.TWO: case CricketUtil.THREE: case CricketUtil.FIVE:
											
											switch (valueToProcess.toUpperCase()) {
											case CricketUtil.DOT: 
												bc.setDots(bc.getDots() + 1);
											}
											this_event.setEventRuns(Integer.valueOf(valueToProcess));
											bc.setRuns(bc.getRuns() + Integer.valueOf(valueToProcess));
											bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + Integer.valueOf(valueToProcess));
											if(inn.getTotalBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver())) {
												inn.setTotalBalls(Integer.valueOf(CricketUtil.DOT));
												inn.setTotalOvers(inn.getTotalOvers() + 1);
												lastBallOfTheOver = true;
											} else {
												inn.setTotalBalls(inn.getTotalBalls() + 1);
											}
											if(bc.getBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver())) {
												bc.setBalls(Integer.valueOf(CricketUtil.DOT));
												bc.setOvers(bc.getOvers() + 1);
												which_bowler = bc.getPlayerId();
											} else {
												bc.setBalls(bc.getBalls() + 1);
											}
											if(session_match.getMatch().getDaysSessions() != null) {
												thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
													ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
												if(thisDaySession != null) {
													for(DaySession ds : bc.getBowlingSession()) {
														if(ds.getDayNumber() == thisDaySession.getDayNumber() 
															&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
															ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess));
															ds.setTotalBalls(ds.getTotalBalls() + 1);
														}
													}
												}
											}									
											for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
												if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
													inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() + 1);
													inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + Integer.valueOf(valueToProcess));
												}
											}
											inn.setTotalRuns(inn.getTotalRuns() + Integer.valueOf(valueToProcess));
											if(session_match.getSetup().getSpecialMatchRules() != null 
												&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
												if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
													if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
														[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
														|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
													{
														if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
															if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
																inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
															} 
//															else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//																inn.setSpecialRuns("0");
//															} 
															else {
																inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
															}
														}
													}
												}
											}													
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() + Integer.valueOf(valueToProcess));
											if(session_match.getMatch().getDaysSessions() != null) {
												for(DaySession ds : session_match.getMatch().getDaysSessions()) {
													if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
														ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess));
														ds.setTotalBalls(ds.getTotalBalls() + 1);
													}
												}
											}
 											inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));
											break;
											
										case CricketUtil.NO_BALL:
											
											this_event.setEventExtra(valueToProcess.toUpperCase());
											
											bc.setRuns(bc.getRuns() + Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
											//bc.setBalls(bc.getBalls() + 1);
											if(session_match.getMatch().getDaysSessions() != null) {
												thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
													ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
												if(thisDaySession != null) {
													for(DaySession ds : bc.getBowlingSession()) {
														if(ds.getDayNumber() == thisDaySession.getDayNumber() 
															&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
															ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
														}
													}
												}
											}									
											bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
											this_event.setEventRuns(Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
											bc.setNoBalls(bc.getNoBalls() + 1);
											inn.setTotalNoBalls(inn.getTotalNoBalls() + 1);
											inn.setTotalRuns(inn.getTotalRuns() + Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
											if(session_match.getSetup().getSpecialMatchRules() != null 
												&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
												if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
													if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
														[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
														|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
													{
														if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
															if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
																inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
															} 
//															else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//																inn.setSpecialRuns("0");
//															} 
															else {
																inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
															}
														}
													}
												}
											}		
											for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
												if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
													inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + 1);
												}
											}
											inn.setTotalExtras(inn.getTotalWides() + inn.getTotalNoBalls() + inn.getTotalByes() 
												+ inn.getTotalLegByes() + inn.getTotalPenalties());
											if(session_match.getMatch().getDaysSessions() != null) {
												for(DaySession ds : session_match.getMatch().getDaysSessions()) {
													if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
														ds.setTotalRuns(ds.getTotalRuns() + 1);
													}
												}
											}
											inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() + 1);
											break;
											
										default:
											
											if (valueToProcess.toUpperCase().contains(CricketUtil.WIDE) && valueToProcess.toUpperCase().contains(",")) 
											{
												this_event.setEventExtra(CricketUtil.WIDE);
												this_event.setEventExtraRuns(Integer.valueOf(valueToProcess.split(",")[1]));
												bc.setRuns(bc.getRuns() + this_event.getEventExtraRuns());
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBowlingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalRuns(ds.getTotalRuns() + this_event.getEventExtraRuns());
															}
														}
													}
												}									
												bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + this_event.getEventExtraRuns());
												this_event.setEventRuns(this_event.getEventExtraRuns());
												bc.setWides(bc.getWides() + this_event.getEventExtraRuns());
												inn.setTotalWides(inn.getTotalWides() + this_event.getEventExtraRuns());
												inn.setTotalRuns(inn.getTotalRuns() + this_event.getEventExtraRuns());
												if(session_match.getSetup().getSpecialMatchRules() != null 
													&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
													if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
														if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
															[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
															|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
														{
															if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
																if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
																	inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
																} 
//																else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//																	inn.setSpecialRuns("0");
//																} 
																else {
																	inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
																}
															}
														}
													}
												}
												for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
													if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
														inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + this_event.getEventExtraRuns());
													}
												}
												inn.setTotalExtras(inn.getTotalWides() + inn.getTotalNoBalls() + inn.getTotalByes() 
													+ inn.getTotalLegByes() + inn.getTotalPenalties());
												if(session_match.getMatch().getDaysSessions() != null) {
													for(DaySession ds : session_match.getMatch().getDaysSessions()) {
														if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
															ds.setTotalRuns(ds.getTotalRuns() + this_event.getEventExtraRuns());
														}
													}
												}
												inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() + this_event.getEventExtraRuns());
											}
											break;
										}
										bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
										this_event.setEventOverNo(inn.getTotalOvers());
										this_event.setEventBallNo(inn.getTotalBalls());

										if(session_match.getSetup().getSpeedFilePath() != null && !session_match.getSetup().getSpeedFilePath().isEmpty()
												&& session_match.getMatch().getCurrent_speed() != null && !session_match.getMatch().getCurrent_speed().isEmpty()) {
											if(bc.getSpeeds() == null) {
												bc.setSpeeds(new ArrayList<Speed>());
											}
											bc.getSpeeds().add(new Speed(bc.getSpeeds().size() + 1, session_match.getMatch().getCurrent_speed(), 
												"", bc.getOvers(), bc.getBalls(), inn.getTotalOvers(), inn.getTotalBalls(), inn.getInningNumber()));
										}
									} 
								}

								for(BattingCard bc:inn.getBattingCard()) {
									if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.NOT_OUT)) {
										switch (valueToProcess.toUpperCase()) {
										case CricketUtil.NO_BALL:
											if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
												this_event.setEventBatterNo(bc.getPlayerId());
												bc.setBalls(bc.getBalls() + 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBattingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalBalls(ds.getTotalBalls() + 1);
															}
														}
													}
												}											
												bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
												if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() + 1);
												} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() + 1);
												}
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);										
												if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
													this_event.setEventBatterNo(bc.getPlayerId());
												} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.NO)) {
													this_event.setEventOtherBatterNo(bc.getPlayerId());
												}
											}
											break;
										case CricketUtil.DOT: case CricketUtil.ONE: case CricketUtil.TWO: case CricketUtil.THREE: case CricketUtil.FIVE: 
											if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
												this_event.setEventBatterNo(bc.getPlayerId());
												bc.setRuns(bc.getRuns() + Integer.valueOf(valueToProcess));
												bc.setBalls(bc.getBalls() + 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBattingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalBalls(ds.getTotalBalls() + 1);
																ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess));
															}
														}
													}
												}											
												bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
												if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() + Integer.valueOf(valueToProcess));
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() + 1);
												} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() + Integer.valueOf(valueToProcess));
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() + 1);
												}
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);										
												if(lastBallOfTheOver == true) {
													switch (valueToProcess.toUpperCase()) {
													case CricketUtil.DOT: case CricketUtil.TWO: 
														bc.setOnStrike(CricketUtil.NO);
														break;
													}
												} else {
													switch (valueToProcess.toUpperCase()) {
													case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
														bc.setOnStrike(CricketUtil.NO);
														break;
													}
												}
											} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.NO)) {
												this_event.setEventOtherBatterNo(bc.getPlayerId());
												if(lastBallOfTheOver == true) {
													switch (valueToProcess.toUpperCase()) {
													case CricketUtil.DOT: case CricketUtil.TWO:  
														bc.setOnStrike(CricketUtil.YES);
														break;
													}
												} else {
													switch (valueToProcess.toUpperCase()) {
													case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
														bc.setOnStrike(CricketUtil.YES);
														break;
													}
												}
											}
											break;
										default:
											if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);										
											}
											if(valueToProcess.toUpperCase().contains(CricketUtil.WIDE)) {
												if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.YES)) {
													this_event.setEventBatterNo(bc.getPlayerId());
													if(lastBallOfTheOver == true) {
														if(valueToProcess.contains(CricketUtil.THREE) || valueToProcess.contains(CricketUtil.FIVE)) {
															bc.setOnStrike(CricketUtil.NO);
														}
													} else {
														if(valueToProcess.contains(CricketUtil.TWO) || valueToProcess.contains(CricketUtil.FOUR)
															|| valueToProcess.contains(CricketUtil.SIX)) {
															bc.setOnStrike(CricketUtil.NO);
														}
													}
												} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.NO)) {
													this_event.setEventOtherBatterNo(bc.getPlayerId());
													if(lastBallOfTheOver == true) {
														if(valueToProcess.contains(CricketUtil.THREE) || valueToProcess.contains(CricketUtil.FIVE)) {
															bc.setOnStrike(CricketUtil.YES);
														}
													} else {
														if(valueToProcess.contains(CricketUtil.TWO) || valueToProcess.contains(CricketUtil.FOUR)
															|| valueToProcess.contains(CricketUtil.SIX)) {
															bc.setOnStrike(CricketUtil.YES);
														}
													}
												}
											}
											break;
										}
									} 
								}
								break;
							}
						}
					}
				}
			}

			session_match.getEventFile().setStatus(CricketFunctions.findConsecutiveDupicateEvents(session_match.getEventFile().getEvents(), this_event));
			session_match.getEventFile().getEvents().add(this_event);
			for(Inning inn : session_match.getMatch().getInning()) {
				if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
					if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
						session_match.getMatch().setMatchStatus(CricketFunctions.GenerateMatchSummaryStatus(
							inn.getInningNumber(), session_match, "", "", "", false).getTargetOrResult());
					}
				}
			}
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.SETUP + "," + CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");
			break;
		}
		return objectMapper.writeValueAsString(session_match);
	}

	public String processLogAnyBallEvent(String whatToProcess,String timeStatsToProcess,String valueToProcess, Event challengeEvent) throws Exception
	{
		boolean lastBallOfTheOver = false; 	
		int which_bowler = 0, total_runs = 0;
		Event this_event = new Event(whatToProcess, valueToProcess);
		DaySession thisDaySession = new DaySession();

		switch (whatToProcess.toUpperCase()) {
		case CricketUtil.LOG_ANY_BALL: 
			
			if(!valueToProcess.trim().isEmpty()) {
				for(Inning inn:session_match.getMatch().getInning()) {
				
					if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
						
						if(session_match.getEventFile() == null)
							session_match.setEventFile(new EventFile());
						if(session_match.getEventFile().getEvents() == null)
							session_match.getEventFile().setEvents(new ArrayList<Event>());
						
						this_event.setEventNumber(session_match.getEventFile().getEvents().size() + 1);
						this_event.setEventType(whatToProcess);
						this_event.setEventInningNumber(inn.getInningNumber());
						this_event.setEventDescription("");
						
						if(Boolean.valueOf(valueToProcess.split(",")[10]) == true)
							this_event.setDoNotIncrementBall(CricketUtil.YES);
						else
							this_event.setDoNotIncrementBall(CricketUtil.NO);
							
						for(BowlingCard bc:inn.getBowlingCard()) {
							if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.CURRENT + CricketUtil.BOWLER)) 
							{
								which_bowler = bc.getPlayerId();
								this_event.setEventBowlerNo(which_bowler);
								if(session_speed != null && session_speed.getSpeedValue() != null && Double.valueOf(session_speed.getSpeedValue()) > 0) {
									this_event.setEventSpeed(session_speed.getSpeedValue());
								}
								this_event.setEventHowOut(valueToProcess.split(",")[1].toUpperCase());

								bc.setRuns(bc.getRuns() + Integer.valueOf(valueToProcess.split(",")[5]));
								if(session_match.getMatch().getDaysSessions() != null) {
									thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
										ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
									if(thisDaySession != null) {
										for(DaySession ds : bc.getBowlingSession()) {
											if(ds.getDayNumber() == thisDaySession.getDayNumber() 
												&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
												ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[5]));
											}
										}
									}
								}
								if(!valueToProcess.split(",")[1].trim().isEmpty())
									this_event.setEventDescription(this_event.getEventDescription() + valueToProcess.split(",")[1]);

								switch (valueToProcess.split(",")[1].toUpperCase()) {
								case CricketUtil.CAUGHT_AND_BOWLED: case CricketUtil.CAUGHT: case CricketUtil.BOWLED: case CricketUtil.STUMPED: 
								case CricketUtil.LBW: case CricketUtil.HIT_WICKET:
									bc.setWickets(bc.getWickets() + 1);
									if(session_match.getMatch().getDaysSessions() != null) {
										thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
											ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
										if(thisDaySession != null) {
											for(DaySession ds : bc.getBowlingSession()) {
												if(ds.getDayNumber() == thisDaySession.getDayNumber() 
													&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
													ds.setTotalWickets(ds.getTotalWickets() + 1);
												}
											}
										}
									}
									for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
										if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
											inn.getSpells().get(i).setWickets(inn.getSpells().get(i).getWickets() + 1);
										}
									}
									if(valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.CAUGHT_AND_BOWLED))
										bc.setCatchAsBowler(bc.getCatchAsBowler() + 1);
									break;
								}
								for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
									if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
										inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + Integer.valueOf(valueToProcess.split(",")[5]));
									}
								}
								if(Integer.valueOf(valueToProcess.split(",")[5]) > 0)
									this_event.setEventDescription(this_event.getEventDescription() + "|" + valueToProcess.split(",")[5]);
								
								this_event.setEventRuns(Integer.valueOf(valueToProcess.split(",")[5]));
								this_event.setEventExtra(valueToProcess.split(",")[0].toUpperCase());

								switch (valueToProcess.split(",")[0].toUpperCase()) {
								case CricketUtil.WIDE: case CricketUtil.NO_BALL:
									this_event.setEventExtraRuns(1);
									switch (valueToProcess.split(",")[0].toUpperCase()) {
									case CricketUtil.WIDE:
										bc.setRuns(bc.getRuns() + 1);
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalRuns(ds.getTotalRuns() + 1);
													}
												}
											}
										}
										bc.setWides(bc.getWides() + 1);
										inn.setTotalWides(inn.getTotalWides() + 1);
										this_event.setEventDescription(this_event.getEventDescription() + "|wd");
										break;
									case CricketUtil.NO_BALL: 
										bc.setRuns(bc.getRuns() + Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(session_match.getSetup().getNoBallsRuns()));
													}
												}
											}
										}
										bc.setNoBalls(bc.getNoBalls() + 1);
										inn.setTotalNoBalls(inn.getTotalNoBalls() + 1);
										this_event.setEventDescription(this_event.getEventDescription() + "|nb");
										break;
									}
									for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
										if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
											inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + 1);
										}
									}
									break;
								}
								
								this_event.setEventSubExtra(valueToProcess.split(",")[7].toUpperCase());
								this_event.setEventSubExtraRuns(Integer.valueOf(valueToProcess.split(",")[8]));
								if(Integer.valueOf(valueToProcess.split(",")[8]) > 0)
									this_event.setEventDescription(this_event.getEventDescription() + "|" + valueToProcess.split(",")[8]);
								
								switch (valueToProcess.split(",")[7].toUpperCase()) {
								case CricketUtil.WIDE: case CricketUtil.NO_BALL:
									switch (valueToProcess.split(",")[7].toUpperCase()) {
									case CricketUtil.WIDE:
										bc.setRuns(bc.getRuns() + Integer.valueOf(valueToProcess.split(",")[8]));
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[8]));
													}
												}
											}
										}
										bc.setWides(bc.getWides() + Integer.valueOf(valueToProcess.split(",")[8]));
										inn.setTotalWides(inn.getTotalWides() + Integer.valueOf(valueToProcess.split(",")[8]));
										this_event.setEventDescription(this_event.getEventDescription() + "|wd");
										break;
									case CricketUtil.NO_BALL: 
										bc.setRuns(bc.getRuns() + Integer.valueOf(valueToProcess.split(",")[8]));
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBowlingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[8]));
													}
												}
											}
										}
										bc.setNoBalls(bc.getNoBalls() + Integer.valueOf(valueToProcess.split(",")[8]));
										inn.setTotalNoBalls(inn.getTotalNoBalls() + Integer.valueOf(valueToProcess.split(",")[8]));
										this_event.setEventDescription(this_event.getEventDescription() + "|nb");
										break;
									}
									for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
										if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
											inn.getSpells().get(i).setRuns(inn.getSpells().get(i).getRuns() + Integer.valueOf(valueToProcess.split(",")[8]));
										}
									}
									break;
								default:
									switch (valueToProcess.split(",")[7].toUpperCase()) {
									case CricketUtil.BYE: 
										inn.setTotalByes(inn.getTotalByes() + Integer.valueOf(valueToProcess.split(",")[8]));
										this_event.setEventDescription(this_event.getEventDescription() + "|b");
										break;
									case CricketUtil.LEG_BYE:
										inn.setTotalLegByes(inn.getTotalLegByes() + Integer.valueOf(valueToProcess.split(",")[8]));
										this_event.setEventDescription(this_event.getEventDescription() + "|lb");
										break;
									case CricketUtil.PENALTY: 
										inn.setTotalPenalties(inn.getTotalPenalties() + Integer.valueOf(valueToProcess.split(",")[8]));
										this_event.setEventDescription(this_event.getEventDescription() + "|p");
										break;
									}
									if (valueToProcess.split(",")[0].trim().isEmpty() // This is normal delivery
											&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.MANKAD)
											&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.TIMED_OUT)
											&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.RETIRED_OUT)
											&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.RETIRED_HURT)
											&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.ABSENT_HURT)) // Count this delivery 
									{
										if(Boolean.valueOf(valueToProcess.split(",")[10]) == false) {
											if(inn.getTotalBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver())) {
												inn.setTotalBalls(Integer.valueOf(CricketUtil.DOT));
												inn.setTotalOvers(inn.getTotalOvers() + 1);
												lastBallOfTheOver = true;
											} else {
												inn.setTotalBalls(inn.getTotalBalls() + 1);
											}
											if(bc.getBalls() + 1 >= Integer.valueOf(session_match.getSetup().getBallsPerOver())) {
												bc.setBalls(Integer.valueOf(CricketUtil.DOT));
												bc.setOvers(bc.getOvers() + 1);
											} else {
												bc.setBalls(bc.getBalls() + 1);
											}
											if(session_match.getMatch().getDaysSessions() != null) {
												thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
													ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
												if(thisDaySession != null) {
													for(DaySession ds : bc.getBowlingSession()) {
														if(ds.getDayNumber() == thisDaySession.getDayNumber() 
															&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
															ds.setTotalBalls(ds.getTotalBalls() + 1);
														}
													}
												}
											}
											for(int i = inn.getSpells().size() - 1; i >= 0; i--) {
												if(inn.getSpells().get(i).getPlayerId() == bc.getPlayerId()) {
													inn.getSpells().get(i).setBalls(inn.getSpells().get(i).getBalls() + 1);
												}
											}
											if(session_match.getMatch().getDaysSessions() != null) {
												for(DaySession ds : session_match.getMatch().getDaysSessions()) {
													if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
														ds.setTotalBalls(ds.getTotalBalls() + 1);
													}
												}
											}
										}
									}
									break;
								}
								this_event.setEventOverNo(inn.getTotalOvers());
								this_event.setEventBallNo(inn.getTotalBalls());
								bc.setEconomyRate(CricketFunctions.generateRunRate(bc.getRuns(),bc.getOvers(),bc.getBalls(),2, session_match));
								
								inn.setTotalExtras(inn.getTotalWides() + inn.getTotalNoBalls() + inn.getTotalByes() + inn.getTotalLegByes() + inn.getTotalPenalties());
								switch (valueToProcess.split(",")[0].toUpperCase()) {
								case CricketUtil.WIDE: case CricketUtil.NO_BALL:
									bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + Integer.valueOf(valueToProcess.split(",")[5]) 
										+ Integer.valueOf(valueToProcess.split(",")[8]) + 1);
									inn.setTotalRuns(inn.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[5]) 
										+ Integer.valueOf(valueToProcess.split(",")[8]) + 1);
									if(session_match.getSetup().getSpecialMatchRules() != null 
										&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
										if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
											if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
												[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
												|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
											{
												if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
													if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
														inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
													} 
//													else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//														inn.setSpecialRuns("0");
//													} 
													else {
														inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
													}
												}
											}
										}
									}									
									if(session_match.getMatch().getDaysSessions() != null) {
										for(DaySession ds : session_match.getMatch().getDaysSessions()) {
											if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
												ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[5]) 
													+ Integer.valueOf(valueToProcess.split(",")[8]) + 1);
											}
										}
									}
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() + 
										Integer.valueOf(valueToProcess.split(",")[5]) + Integer.valueOf(valueToProcess.split(",")[8]) + 1);
									break;
								default:
									if(valueToProcess.split(",")[7] != null && (valueToProcess.split(",")[7].equalsIgnoreCase(CricketUtil.BYE)
										|| valueToProcess.split(",")[7].equalsIgnoreCase(CricketUtil.LEG_BYE))) {
										bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + Integer.valueOf(valueToProcess.split(",")[5]));
									} else {
										bc.setTotalRunsThisOver(bc.getTotalRunsThisOver() + Integer.valueOf(valueToProcess.split(",")[5]) 
											+ Integer.valueOf(valueToProcess.split(",")[8]));
									}
									inn.setTotalRuns(inn.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[5]) + Integer.valueOf(valueToProcess.split(",")[8]));
									if(session_match.getMatch().getDaysSessions() != null) {
										for(DaySession ds : session_match.getMatch().getDaysSessions()) {
											if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
												ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[5]) + Integer.valueOf(valueToProcess.split(",")[8]));
											}
										}
									}
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalRuns(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalRuns() + 
										Integer.valueOf(valueToProcess.split(",")[5]) + Integer.valueOf(valueToProcess.split(",")[8]));
									break;
								}
								if(session_match.getSetup().getSpecialMatchRules() != null 
									&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
									if(bc.getBallTypeOverNo() != null && !bc.getBallTypeOverNo().isEmpty()) {
										if((bc.getBallTypeOverNo().contains(",") && bc.getBallTypeOverNo().split(",")
											[bc.getBallTypeOverNo().split(",").length-1].equalsIgnoreCase("CHALLENGE"))
											|| bc.getBallTypeOverNo().equalsIgnoreCase("CHALLENGE")) 
										{
											if(challengeEvent != null && challengeEvent.getEventBowlerNo() == bc.getPlayerId()) {
												if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) >= 0) {
													inn.setSpecialRuns("+" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
												} 
//												else if((inn.getTotalRuns() - challengeEvent.getEventRuns()) - Integer.parseInt(challengeEvent.getEventSubExtra()) == 0) {
//													inn.setSpecialRuns("0");
//												} 
												else {
													inn.setSpecialRuns("-" + String.valueOf((inn.getTotalRuns() - challengeEvent.getEventRuns()) / 2));
												}
											}
										}
									}
								}	
								int partnershipBalls = 0;
								if(Integer.valueOf(valueToProcess.split(",")[5]) > 0 || Integer.valueOf(valueToProcess.split(",")[8]) > 0) {
									partnershipBalls = 1;
								} else {
									switch (valueToProcess.split(",")[0].toUpperCase()) {
									case CricketUtil.WIDE: case CricketUtil.NO_BALL:
										partnershipBalls = 1;
										break;
									default:
										//How out text found so increment part total balls
										if(!valueToProcess.split(",")[1].trim().isEmpty()) {
											switch (valueToProcess.split(",")[1].toUpperCase()) {
											case CricketUtil.MANKAD: case CricketUtil.RETIRED_HURT: case CricketUtil.RETIRED_OUT: 
											case CricketUtil.TIMED_OUT: case CricketUtil.ABSENT_HURT:
												break;
											default:
												partnershipBalls = 1;
												break;
											}
										} else {
											switch (valueToProcess.split(",")[7].toUpperCase()) {
											case CricketUtil.WIDE: case CricketUtil.NO_BALL:
												partnershipBalls = 1;
												break;
											}
										}
										break;
									}
								}
								if(Boolean.valueOf(valueToProcess.split(",")[10]) == false) {
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + partnershipBalls);
								}
//								if (!valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.MANKAD)
//										&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.TIMED_OUT)
//										&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.RETIRED_OUT)
//										&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.RETIRED_HURT)) {
//									if(Boolean.valueOf(valueToProcess.split(",")[10]) == false) {
//										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
//											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);
//									}
//								}
								
								inn.setRunRate(CricketFunctions.generateRunRate(inn.getTotalRuns(),inn.getTotalOvers(),inn.getTotalBalls(),2, session_match));

								switch (valueToProcess.split(",")[0].toUpperCase()) {
								case CricketUtil.WIDE: case CricketUtil.NO_BALL:
									break;
								default:
									switch (valueToProcess.split(",")[7].toUpperCase()) {
									case CricketUtil.WIDE: case CricketUtil.NO_BALL: case CricketUtil.PENALTY:
										break;
									case CricketUtil.BYE: case CricketUtil.LEG_BYE:
										switch (valueToProcess.split(",")[1].toUpperCase()) {
										case CricketUtil.RUN_OUT: case CricketUtil.OBSTRUCTING_FIELDER: 
											bc.setDots(bc.getDots() + 1);
											break;
										}
										break;
									default:
										if(this_event.getEventRuns() <= 0 && this_event.getEventExtraRuns() <= 0 && this_event.getEventSubExtraRuns() <= 0) {
											bc.setDots(bc.getDots() + 1);
										}
										break;
									}
								}
							
								if(session_match.getSetup().getSpeedFilePath() != null && !session_match.getSetup().getSpeedFilePath().isEmpty()
										&& session_match.getMatch().getCurrent_speed() != null && !session_match.getMatch().getCurrent_speed().isEmpty()) {
									if(bc.getSpeeds() == null) {
										bc.setSpeeds(new ArrayList<Speed>());
									}
									bc.getSpeeds().add(new Speed(bc.getSpeeds().size() + 1, session_match.getMatch().getCurrent_speed(), 
										"", bc.getOvers(), bc.getBalls(), inn.getTotalOvers(), inn.getTotalBalls(), inn.getInningNumber()));
								}

							} else if(bc.getStatus() != null && bc.getStatus().equalsIgnoreCase(CricketUtil.OTHER + CricketUtil.BOWLER)) {
								this_event.setEventOtherBowlerNo(bc.getPlayerId());
							}
						}
						for(BattingCard bc:inn.getBattingCard()) {
							total_runs = Integer.valueOf(valueToProcess.split(",")[5]) + Integer.valueOf(valueToProcess.split(",")[8]);
							if(bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[4])) {
								this_event.setEventBatterNo(bc.getPlayerId());
								bc.setRuns(bc.getRuns() + Integer.valueOf(valueToProcess.split(",")[5]));
								this_event.setEventOnStrike(bc.getOnStrike());
								//bc.setOnStrike("");
								if(session_match.getMatch().getDaysSessions() != null) {
									thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
										ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
									if(thisDaySession != null) {
										for(DaySession ds : bc.getBattingSession()) {
											if(ds.getDayNumber() == thisDaySession.getDayNumber() 
												&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
												ds.setTotalRuns(ds.getTotalRuns() + Integer.valueOf(valueToProcess.split(",")[5]));
											}
										}
									}
								}								
								bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
								if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterRuns(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterRuns() + Integer.valueOf(valueToProcess.split(",")[5]));
								} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterRuns(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterRuns() + Integer.valueOf(valueToProcess.split(",")[5]));
								}
								if (valueToProcess.split(",")[5].equalsIgnoreCase(CricketUtil.FOUR) 
									&& valueToProcess.split(",")[6].equalsIgnoreCase(CricketUtil.BOUNDARY)) {
									this_event.setEventWasABoundary(CricketUtil.YES);
									bc.setFours(bc.getFours() + 1);
									inn.setTotalFours(inn.getTotalFours() + 1);
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalFours(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalFours() + 1);
									if(session_match.getMatch().getDaysSessions() != null) {
										for(DaySession ds : session_match.getMatch().getDaysSessions()) {
											if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
												ds.setTotalFours(ds.getTotalFours() + 1);
												for(DaySession bds : bc.getBattingSession()) {
													if(bds.getDayNumber() == ds.getDayNumber() 
														&& bds.getSessionNumber() == ds.getSessionNumber()) {
														bds.setTotalFours(ds.getTotalFours() + 1);
													}
												}
											}
										}
									}
								} else if (valueToProcess.split(",")[5].equalsIgnoreCase(CricketUtil.SIX) 
									&& valueToProcess.split(",")[6].equalsIgnoreCase(CricketUtil.BOUNDARY)) {
									this_event.setEventWasABoundary(CricketUtil.YES);
									bc.setSixes(bc.getSixes() + 1);
									inn.setTotalSixes(inn.getTotalSixes() + 1);
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalSixes(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalSixes() + 1);
									if(session_match.getMatch().getDaysSessions() != null) {
										for(DaySession ds : session_match.getMatch().getDaysSessions()) {
											if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
												ds.setTotalSixes(ds.getTotalSixes() + 1);
												for(DaySession bds : bc.getBattingSession()) {
													if(bds.getDayNumber() == ds.getDayNumber() 
														&& bds.getSessionNumber() == ds.getSessionNumber()) {
														bds.setTotalSixes(ds.getTotalSixes() + 1);
													}
												}
											}
										}
									}
								} else if (valueToProcess.split(",")[5].equalsIgnoreCase(CricketUtil.NINE) 
									&& valueToProcess.split(",")[6].equalsIgnoreCase(CricketUtil.BOUNDARY)) {
									this_event.setEventWasABoundary(CricketUtil.YES);
									bc.setNines(bc.getNines() + 1);
									inn.setTotalNines(inn.getTotalNines() + 1);
									inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalNines(
										inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalNines() + 1);
								}
								switch (valueToProcess.split(",")[7].toUpperCase()) {
								case CricketUtil.PENALTY: 
									break;
								default:
									if(lastBallOfTheOver == true) {
										switch (String.valueOf(total_runs)) {
										case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: case CricketUtil.NINE:
											bc.setOnStrike(CricketUtil.NO);
											break;
										}
									} else {
										switch (String.valueOf(total_runs)) {
										case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
											bc.setOnStrike(CricketUtil.NO);
											break;
										}
									}
								}
								switch (valueToProcess.split(",")[0].trim().toUpperCase()) {
								case CricketUtil.WIDE: 
									break;
								case CricketUtil.NO_BALL:
									if(Boolean.valueOf(valueToProcess.split(",")[10]) == false) {
										bc.setBalls(bc.getBalls() + 1);
										if(session_match.getMatch().getDaysSessions() != null) {
											thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
												ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
											if(thisDaySession != null) {
												for(DaySession ds : bc.getBattingSession()) {
													if(ds.getDayNumber() == thisDaySession.getDayNumber() 
														&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
														ds.setTotalBalls(ds.getTotalBalls() + 1);
													}
												}
											}
										}
										if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() + 1);
										} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
											inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
												inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() + 1);
										}
//										inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
//											inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);										
									}
									bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
									break;
								default:
									switch (valueToProcess.split(",")[7].trim().toUpperCase()) {
									case CricketUtil.WIDE: 
										break;
									case CricketUtil.NO_BALL:
										if(Boolean.valueOf(valueToProcess.split(",")[10]) == false) {
											bc.setBalls(bc.getBalls() + 1);
											if(session_match.getMatch().getDaysSessions() != null) {
												thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
													ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
												if(thisDaySession != null) {
													for(DaySession ds : bc.getBattingSession()) {
														if(ds.getDayNumber() == thisDaySession.getDayNumber() 
															&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
															ds.setTotalBalls(ds.getTotalBalls() + 1);
														}
													}
												}
											}
											if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() + 1);
											} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
												inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
													inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() + 1);
											}
//											inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
//												inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);										
										}
										bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
										break;
									default:
										if (!valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.RETIRED_HURT) 
											&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.ABSENT_HURT)
											&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.TIMED_OUT)
											&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.RETIRED_OUT)
											&& !valueToProcess.split(",")[1].equalsIgnoreCase(CricketUtil.MANKAD)) {
											if(Boolean.valueOf(valueToProcess.split(",")[10]) == false) {
												bc.setBalls(bc.getBalls() + 1);
												if(session_match.getMatch().getDaysSessions() != null) {
													thisDaySession = session_match.getMatch().getDaysSessions().stream().filter(
														ds -> ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)).findAny().orElse(null);
													if(thisDaySession != null) {
														for(DaySession ds : bc.getBattingSession()) {
															if(ds.getDayNumber() == thisDaySession.getDayNumber() 
																&& ds.getSessionNumber() == thisDaySession.getSessionNumber()) {
																ds.setTotalBalls(ds.getTotalBalls() + 1);
															}
														}
													}
												}												
												if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setFirstBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getFirstBatterBalls() + 1);
												} else if(inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterNo() == bc.getPlayerId()) {
													inn.getPartnerships().get(inn.getPartnerships().size()-1).setSecondBatterBalls(
														inn.getPartnerships().get(inn.getPartnerships().size()-1).getSecondBatterBalls() + 1);
												}
//												inn.getPartnerships().get(inn.getPartnerships().size()-1).setTotalBalls(
//													inn.getPartnerships().get(inn.getPartnerships().size()-1).getTotalBalls() + 1);										
											}
											bc.setStrikeRate(CricketFunctions.generateStrikeRate(bc.getRuns(),bc.getBalls(),1));
										}
										break;
									}
									break;
								}
							} else if(bc.getOnStrike() != null && bc.getOnStrike().equalsIgnoreCase(CricketUtil.NO)) 
							{
								this_event.setEventOtherBatterNo(bc.getPlayerId());
								switch (valueToProcess.split(",")[7].toUpperCase()) {
								case CricketUtil.PENALTY: 
									break;
								default:
									if(bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[2])
										&& !valueToProcess.split(",")[1].trim().isEmpty()) 
									{
										//Keep ON-STRIKE as NO for OUT batsman
										bc.setOnStrike(CricketUtil.NO);
									} else {
										if(lastBallOfTheOver == true) {
											switch (String.valueOf(total_runs)) {
											case CricketUtil.TWO: case CricketUtil.FOUR: case CricketUtil.SIX: case CricketUtil.NINE:
												bc.setOnStrike(CricketUtil.YES);
												break;
											}
										} else {
											switch (String.valueOf(total_runs)) {
											case CricketUtil.ONE: case CricketUtil.THREE: case CricketUtil.FIVE:
												bc.setOnStrike(CricketUtil.YES);
												break;
											}
										}
									}
									break;
								}
							}
						}
						for(BattingCard bc:inn.getBattingCard()) {
							if(bc.getPlayerId() == Integer.valueOf(valueToProcess.split(",")[2])) {
								if(!valueToProcess.split(",")[1].trim().isEmpty()) { // How out text found
									this_event.setEventHowOutBatterNo(bc.getPlayerId());
									bc.setOnStrike("");
									switch (valueToProcess.split(",")[1].toUpperCase()) {
									case CricketUtil.RETIRED_HURT:
										bc.setStatus(CricketUtil.STILL_TO_BAT);
										break;
									default:
										bc.setStatus(CricketUtil.OUT);
										inn.setTotalWickets(inn.getTotalWickets() + 1);
										if(session_match.getMatch().getDaysSessions() != null) {
											for(DaySession ds : session_match.getMatch().getDaysSessions()) {
												if(ds.getIsCurrentSession().equalsIgnoreCase(CricketUtil.YES)) {
													ds.setTotalWickets(ds.getTotalWickets() + 1);
												}
											}
										}
										if(inn.getFallsOfWickets() == null || inn.getFallsOfWickets().size() <= 0) {
											inn.setFallsOfWickets(new ArrayList<FallOfWicket>());
										}
										if(session_match.getSetup().getSpecialMatchRules() != null 
											&& session_match.getSetup().getSpecialMatchRules().equalsIgnoreCase(CricketUtil.ISPL)) {
											inn.getFallsOfWickets().add(new FallOfWicket(inn.getFallsOfWickets().size() + 1, bc.getPlayerId(),
										        inn.getTotalRuns() + (inn.getSpecialRuns() == null ? 0 : Integer.parseInt(inn.getSpecialRuns().trim())),
										        inn.getTotalOvers(), inn.getTotalBalls(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))));
										} else {
											inn.getFallsOfWickets().add(new FallOfWicket(inn.getFallsOfWickets().size() + 1, 
												bc.getPlayerId(), inn.getTotalRuns(), inn.getTotalOvers(), inn.getTotalBalls(),
												LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))));
										}										
										break;
									}
									bc.setHowOut(valueToProcess.split(",")[1]);
									bc.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
									switch (valueToProcess.split(",")[1].toUpperCase()) {
									case CricketUtil.CAUGHT_AND_BOWLED: case CricketUtil.CAUGHT: case CricketUtil.BOWLED: 
									case CricketUtil.STUMPED: case CricketUtil.LBW: case CricketUtil.HIT_WICKET: case CricketUtil.MANKAD:
										bc.setHowOutBowlerId(which_bowler);
										break;
									}
									switch (valueToProcess.split(",")[1].toUpperCase()) {
									case CricketUtil.CAUGHT: case CricketUtil.RUN_OUT: case CricketUtil.MANKAD: case CricketUtil.STUMPED:
										bc.setHowOutFielderId(Integer.valueOf(valueToProcess.split(",")[3]));
										this_event.setEventHowOutFielderId(Integer.valueOf(valueToProcess.split(",")[3]));
										bc.setWasHowOutFielderSubstitute(valueToProcess.split(",")[11]);
										this_event.setSubstitutionMade(valueToProcess.split(",")[11]);
										break;
									}
									bc=CricketFunctions.processBattingcard(cricketService,bc);
								}
							}
						}
						switch (valueToProcess.split(",")[1].toUpperCase()) {
						case CricketUtil.CONCUSSED:
							if(valueToProcess.split(",").length >= 10) {
								inn.getBattingCard().add(CricketFunctions.processBattingcard(cricketService,
									new BattingCard(Integer.valueOf(valueToProcess.split(",")[9]),
									inn.getBattingCard().size() + 1, CricketUtil.STILL_TO_BAT)));
								this_event.setEventConcussionReplacePlayerId(Integer.valueOf(valueToProcess.split(",")[9]));
							}
							break;
						}
					}
				}
			}

			for(Inning inn : session_match.getMatch().getInning()) {
				if(inn.getIsCurrentInning() != null && inn.getIsCurrentInning().equalsIgnoreCase(CricketUtil.YES)) {
					if(!session_match.getMatch().getSourceOfThisMatchData().equalsIgnoreCase(CricketUtil.WEBSITE)) {
						session_match.getMatch().setMatchStatus(CricketFunctions.GenerateMatchSummaryStatus(
							inn.getInningNumber(), session_match, "", "", "", false).getTargetOrResult());
					}
				}
			}
			
			session_match.getEventFile().setStatus(CricketFunctions.findConsecutiveDupicateEvents(session_match.getEventFile().getEvents(), this_event));
			session_match.getEventFile().getEvents().add(this_event);
			session_match.setMatch(CricketFunctions.processInningTimeData("PROCESS_TIME_STATS",session_match.getMatch(), timeStatsToProcess,this_event));
			//last_match_data = objectMapper.readValue(objectMapper.writeValueAsString(session_match.getMatch()), Match.class);
			//CricketFunctions.getInteractive(CricketFunctions.getAllEventsStats(session_match.getMatch(), session_match.getEventFile().getEvents()));
			CricketFunctions.readOrSaveMatchFile(CricketUtil.WRITE,CricketUtil.SETUP + "," + CricketUtil.MATCH + "," + CricketUtil.EVENT,session_match, session_config);
			//CricketFunctions.getInteractive(session_match, "FULL_WRITE");
			break;
		}
		return objectMapper.writeValueAsString(session_match);
	}
}