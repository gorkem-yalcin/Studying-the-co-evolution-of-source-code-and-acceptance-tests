import matplotlib.pyplot as plt
import pandas as pd
from sklearn import preprocessing

data = pd.read_csv('namesimplified.csv')
colors = ["red", "orange", "black", "blue", "green", "cyan"]
charts = ["name",
          "version_count",
          "all_version_test_over_all",
          "all_version_only_test_over_all",
          "all_version_only_source_over_all",
          "all_version_source_and_test_over_all",
          "major_minor_version_count",
          "file_change_count",
          "sloc",
          "tloc",
          "major_minor_test_over_all",
          "major_minor_source_over_all"]
project_idx_list = []
project_name_list = []
version_count_list = []
all_version_test_over_all_list = []
all_version_only_test_over_all_list = []
all_version_only_prod_over_all_list = []
all_version_prod_and_test_over_all_list = []
major_minor_version_count_list = []
file_change_count_list = []
eloc_list = []
tloc_list = []
major_minor_test_over_all_list = []
major_minor_prod_over_all_list = []
scaler = preprocessing.MinMaxScaler()
for i, project in enumerate(data.values):
    project_name_list.append(project[0])
    version_count_list.append(project[1])
    all_version_test_over_all_list.append(project[2])
    all_version_only_test_over_all_list.append(project[3])
    all_version_only_prod_over_all_list.append(project[4])
    all_version_prod_and_test_over_all_list.append(project[5])
    major_minor_version_count_list.append(project[6])
    file_change_count_list.append(project[7])
    eloc_list.append(project[8])
    tloc_list.append(project[9])
    major_minor_test_over_all_list.append(project[10])
    major_minor_prod_over_all_list.append(project[11])
    project_idx_list.append(project[12])

plt.bar(project_idx_list, tloc_list, color=colors)
plt.title('Projects x ' + "tloc_list")
plt.xlabel('Projects')
plt.ylabel(tloc_list)
plt.savefig("color Bar graph Projects x " + tloc_list, bbox_inches='tight')
