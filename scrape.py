import os
from ast import literal_eval
import csv

root_path = "log_files"

all_data = [["txt_num", "game", "day", "campaign", "quality_score", "cumulative_profit"]]
start_kw = "Starting a new Game, game #"
day_kw = "Day: "
stats_kw = "Statistics: "
campaign_kw = "[startDay ="
qs_hw = "Quality Score"
profit_kw = "Cumulative Profit: "

folders = [i for i in os.listdir(root_path) if i[0] != "."]
print(folders)
for folder in folders:
    file_names = os.listdir(os.path.join(root_path, folder))
    for file_ in file_names:
        full_path = os.path.join(root_path, folder, file_)
        txt_num = file_.split(".")[0]

        with open(full_path, "r") as file:
            data_ = file.readlines()

            curr_game = 0
            curr_day = 0

            for line_ in data_:
                if start_kw in line_:
                    curr_game = literal_eval(
                        line_[line_.index(start_kw)+len(start_kw):])
                if day_kw in line_:
                    curr_day = literal_eval(
                        line_[line_.index(day_kw)+len(day_kw):-2])
                if stats_kw in line_:
                    if "null" in line_:
                        curr_stats = None
                    else:
                        curr_stats = line_[line_.index(stats_kw) + len(stats_kw):-1]
                if campaign_kw in line_:
                    curr_campaign = line_[line_.index(campaign_kw):]
                if qs_hw in line_:
                    curr_qs = line_[line_.index(qs_hw) + len(qs_hw):-1]
                if profit_kw in line_:
                    curr_profit = line_[line_.index(profit_kw) + len(profit_kw):-1]
                    all_data.append([txt_num, curr_game, curr_day, curr_stats, curr_campaign, curr_qs, curr_profit])

with open("output.csv", "w") as f:
    writer = csv.writer(f)
    writer.writerows(all_data)
