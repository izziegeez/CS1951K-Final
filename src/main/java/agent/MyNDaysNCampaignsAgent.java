package agent;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.ImmutableMap;

import adx.agent.AgentLogic;
import adx.exceptions.AdXException;
import adx.server.OfflineGameServer;
import adx.structures.SimpleBidEntry;
import adx.util.AgentStartupUtil;
import adx.structures.Campaign;
import adx.structures.MarketSegment;
import adx.variants.ndaysgame.NDaysAdBidBundle;
import adx.variants.ndaysgame.NDaysNCampaignsAgent;
import adx.variants.ndaysgame.NDaysNCampaignsGameServerOffline;
import adx.variants.ndaysgame.Tier1NDaysNCampaignsAgent;

public class MyNDaysNCampaignsAgent extends NDaysNCampaignsAgent {
	private static final String NAME = "lwei5-ngao"; // TODO: enter a name. please remember to submit the Google form.

	public MyNDaysNCampaignsAgent() {
		// TODO: fill this in (if necessary)
	}
	
	@Override
	protected void onNewGame() {
		// TODO: fill this in (if necessary)
	}
	
	@Override
	protected Set<NDaysAdBidBundle> getAdBids() throws AdXException {
		
		Set<NDaysAdBidBundle> bundles = new HashSet<>();
		
		for (Campaign c : this.getActiveCampaigns()) {
			double bid;
		    if(this.getCumulativeReach(c) < 0.8 && c.getEndDay() - this.getCurrentDay() <= 1) {
		    	bid = c.getBudget() / c.getReach();
		    }else {
		    	bid = c.getBudget() / c.getReach() * this.get_shade_factor();
		    }
			SimpleBidEntry bidEntry = new SimpleBidEntry(c.getMarketSegment(), bid, c.getBudget());
			Set<SimpleBidEntry> simpleBidEntries = new HashSet<>();
			simpleBidEntries.add(bidEntry);
			NDaysAdBidBundle new_bundle = new NDaysAdBidBundle(c.getId(), c.getBudget(), simpleBidEntries);
			bundles.add(new_bundle);
		}
		
		return bundles;
	}

	@Override
	protected Map<Campaign, Double> getCampaignBids(Set<Campaign> campaignsForAuction) throws AdXException {
		
		Map<Campaign, Double> bids = new HashMap<>();
		
		for (Campaign c : campaignsForAuction) {
			bids.put(c, this.get_learned_price_index(c.getMarketSegment())*c.getReach());
		}
		
		return bids;
	}
	
	public double get_learned_price_index(MarketSegment m) {
		return 0.3;
	}
	
	public double get_shade_factor() {
		return 0.25;
	}
	
//	public double get_allocation_and_payment(MarketSegment m, int k) {
//		int curr_supply = MarketSegment.proportionsMap.get(m);
//		int x = 0;
//		double p = 0.0;
//		for(int i = 0; i <= k; i++) {
////			if(x < curr_supply) {
////				x = x;
////			}
//		}
//		return 0.0;
//	}

	public static void main(String[] args) throws IOException, AdXException {
		// Here's an opportunity to test offline against some TA agents. Just run
		// this file in Eclipse to do so.
		// Feel free to change the type of agents.
		// Note: this runs offline, so:
		// a) It's much faster than the online test; don't worry if there's no delays.
		// b) You should still run the test script mentioned in the handout to make sure
		// your agent works online.
		if (args.length == 0) {
			Map<String, AgentLogic> test_agents = new ImmutableMap.Builder<String, AgentLogic>()
					.put("me", new MyNDaysNCampaignsAgent())
					.put("opponent_1", new Tier1NDaysNCampaignsAgent())
					.put("opponent_2", new Tier1NDaysNCampaignsAgent())
					.put("opponent_3", new Tier1NDaysNCampaignsAgent())
					.put("opponent_4", new Tier1NDaysNCampaignsAgent())
					.put("opponent_5", new Tier1NDaysNCampaignsAgent())
					.put("opponent_6", new Tier1NDaysNCampaignsAgent())
					.put("opponent_7", new Tier1NDaysNCampaignsAgent())
					.put("opponent_8", new Tier1NDaysNCampaignsAgent())
					.put("opponent_9", new Tier1NDaysNCampaignsAgent()).build();

			// Don't change this.
			OfflineGameServer.initParams(new String[] { "offline_config.ini", "CS1951K-FINAL" });
			AgentStartupUtil.testOffline(test_agents, new NDaysNCampaignsGameServerOffline());
		} else {
			// Don't change this.
			AgentStartupUtil.startOnline(new MyNDaysNCampaignsAgent(), args, NAME);
		}
	}

}
