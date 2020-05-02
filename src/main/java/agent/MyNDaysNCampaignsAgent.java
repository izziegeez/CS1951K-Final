package agent;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
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
	private Set<Campaign> allCampaigns;
	
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
			double bid = this.get_utility_maximizing_bid(c);
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
		this.allCampaigns = campaignsForAuction;
		
		for (Campaign c : campaignsForAuction) {
			bids.put(c, this.get_learned_price_index(c.getMarketSegment())*c.getReach());
		}
		
		return bids;
	}
	
	public double get_learned_price_index(MarketSegment m) {
		HashMap<String, Double> market_priceindex_lookup = new HashMap<String, Double>(){
			{
				put("FEMALE_HIGH_INCOME", 0.1725973382129604);
				put("FEMALE_LOW_INCOME", 0.16272734690073507);
				put("FEMALE_OLD", 0.159168408781004);
				put("FEMALE_OLD_HIGH_INCOME", 0.199643364613246);
				put("FEMALE_OLD_LOW_INCOME", 0.23007089761341973);
				put("FEMALE_YOUNG", 0.21029370908681825);
				put("FEMALE_YOUNG_HIGH_INCOME", 0.2452796419787934);
				put("FEMALE_YOUNG_LOW_INCOME", 0.21504007991352573);
				put("FEMALE_HIGH_INCOME", 0.1943665425899583);
				put("MALE_HIGH_INCOME", 0.1943665425899583);
				put("MALE_LOW_INCOME", 0.2022844394912688);
				put("MALE_OLD", 0.1774527289563977);
				put("MALE_OLD_HIGH_INCOME", 0.22291884677035093);
				put("MALE_OLD_LOW_INCOME", 0.2071734228588909);
				put("MALE_YOUNG", 0.2024178329232215);
				put("MALE_YOUNG_HIGH_INCOME", 0.23670612474460315);
				put("MALE_YOUNG_LOW_INCOME", 0.22195648414764966);
				put("OLD_HIGH_INCOME", 0.2086182161049369);
				put("OLD_LOW_INCOME", 0.18692185865409875);
				put("YOUNG_HIGH_INCOME", 0.18851944966548354);
				put("YOUNG_LOW_INCOME", 0.18686355846427);
			}
		};
		if(market_priceindex_lookup.containsKey(m.name())) {
			return market_priceindex_lookup.get(m.name());
		}else {
			return 0.2;
		}
	}
	
	public double get_shade_factor() {
		return 0.6;
	}
	
	public double get_utility_maximizing_bid(Campaign my_c) throws AdXException {
		double curr_price = my_c.getBudget() / my_c.getReach();
		MarketSegment m = my_c.getMarketSegment();
		Map<Double, Campaign> lookup = new HashMap<>();
		List<Double> allBidsForMarket = new ArrayList<Double>();
		for (Campaign c : this.allCampaigns) {
			// we only consider this campaign if its market segment 
			// is a superset of the segment we are interested in
			if(MarketSegment.marketSegmentSubset(c.getMarketSegment(), m) ) {
				double bid = this.get_learned_price_index(c.getMarketSegment());
				allBidsForMarket.add(bid);
				lookup.put(bid, c);
			}
		}
		Collections.sort(allBidsForMarket, Collections.reverseOrder());
		
		double best_bid = 0;
		double max_profit = 0.0;
		// loop through all the bid ranks
		for(int k = 0; k <= allBidsForMarket.size(); k++) {
			
			// we insert the bid into the kth position and see what our payment and allocation would become
			int curr_supply = MarketSegment.proportionsMap.get(m);
			int x_k;
			double payment_k;
			double bid_k;
			
			// if k is the biggest
			if(k == 0) {
				bid_k = curr_price * this.get_shade_factor();
			// k is not the biggest
			}else {
				// k is smaller than the last item in the list
				if (k == allBidsForMarket.size()) {
					bid_k = 0;
				// k is not smaller than the last item and also not the first item
				}else {
					bid_k = (allBidsForMarket.get(k) + allBidsForMarket.get(k - 1)) / 2;
				}
			}
			
			// go from the highest bid to the lowest bid
			for(int i = 0; i < k; i++) {
				double bid = allBidsForMarket.get(i);
				// get the potential allocation of the ith rank agent
				int x_i = Math.min(lookup.get(bid).getReach(), curr_supply);
				curr_supply -= x_i;
			}
			
			// calculate the allocation left for us
			if (bid_k == 0) {
				x_k = curr_supply;
			}else {
				x_k = Math.min((int)(my_c.getBudget() / bid_k), curr_supply);
			}
			
			// calculate the payment for us
			// k is the last, payment would just be zero
			if(k == allBidsForMarket.size()) {
				payment_k = 0;
			}else {
				// the k_th bid in the list is actually the second price that k pays because we did not actually insert
				payment_k = x_k * allBidsForMarket.get(k);
			}
			
			// then, we calculate the utility under this payment and allocation
			double curr_profit = NDaysNCampaignsAgent.effectiveReach(x_k, my_c.getReach()) * my_c.getBudget() - payment_k;
			if (curr_profit > max_profit ){
				max_profit = curr_profit;
				best_bid = bid_k;
			}
		}
		System.out.println(best_bid);
		return best_bid;
	}

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
