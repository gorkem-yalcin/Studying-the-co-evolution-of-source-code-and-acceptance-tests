import seaborn as sns
from sklearn.cluster import KMeans
import matplotlib.pyplot as plt
import pandas as pd
from math import pi
import numpy as np
from sklearn import preprocessing

column_name = "NAME"  # 0
column_version_count = "VC"  # 1
column_all_version_test_all = "AVTOA"  # 2
column_all_version_only_test_all = "AVOTOA"  # 3
column_all_version_only_source_all = "AVOSOA"  # 4
column_all_version_source_and_test_all = "AVSATOA"  # 5
column_major_minor_version_count = "MMVC"  # 6
column_file_change_count = "FCC"  # 7
column_sloc = "SLOC"  # 8
column_tloc = "TLOC"  # 9
column_major_minor_test_all = "MMTOA"  # 10
column_major_minor_prod_all = "MMSOA"  # 11

column_list = [column_name, column_version_count, column_all_version_test_all, column_all_version_only_test_all,
               column_all_version_only_source_all, column_all_version_source_and_test_all,
               column_major_minor_version_count,
               column_file_change_count, column_sloc, column_tloc, column_major_minor_test_all,
               column_major_minor_prod_all]
combinations = [[1, 2, 3], [1, 3, 3], [1, 4, 3], [1, 5, 3], [1, 6, 4], [1, 7, 3], [1, 8, 3], [1, 9, 3], [1, 10, 3],
                [1, 11, 3], [2, 3, 4], [2, 4, 3], [2, 5, 3], [2, 6, 3], [2, 7, 3], [2, 8, 3], [2, 9, 3], [2, 10, 3],
                [2, 11, 4], [3, 4, 4], [3, 5, 4], [3, 6, 3], [3, 7, 3], [3, 8, 3], [3, 9, 3], [3, 10, 5], [3, 11, 2],
                [4, 5, 4], [4, 6, 3], [4, 7, 3], [4, 8, 3], [4, 9, 3], [4, 10, 3], [4, 11, 4], [5, 6, 3], [5, 7, 3],
                [5, 8, 4], [5, 9, 3], [5, 10, 2], [5, 11, 4], [6, 7, 3], [6, 8, 3], [6, 9, 3], [6, 10, 3], [6, 11, 3],
                [7, 8, 3], [7, 9, 3], [7, 10, 3], [7, 11, 3], [8, 9, 3], [8, 10, 3], [8, 11, 3], [9, 10, 3], [9, 11, 3],
                [10, 11, 4]]
# Elbow Graph Creation
for comb in combinations:
    xaxis = column_list[comb[0]]
    yaxis = column_list[comb[1]]

    sns.set()
    chosen_array = [column_list.index(xaxis), column_list.index(yaxis)]

    data = pd.read_csv('data-outliers-removed.csv')
    x = data.iloc[:, chosen_array]

    wcss = []
    for i in range(1, 10):
        kmeans = KMeans(i)
        kmeans.fit(x)
        wcss_iter = kmeans.inertia_
        wcss.append(wcss_iter)

    number_clusters = range(1, 10)
    plt.plot(number_clusters, wcss)
    plt.title(xaxis + " - " + yaxis, fontsize=20)
    plt.xlabel('Number of clusters', fontsize=20)
    plt.ylabel('WCSS', fontsize=20)
    plt.savefig("Elbow - " + column_list[chosen_array[0]] + " - " + column_list[chosen_array[1]], bbox_inches='tight')

    # Cluster Graph Creation
    fig = plt.figure()
    plt.figure().clear()
    plt.close()
    plt.cla()
    plt.clf()
    kmeans = KMeans(comb[2])
    kmeans.fit(x)
    identified_clusters = kmeans.fit_predict(x)
    data_with_clusters = data.copy()
    data_with_clusters['Clusters'] = identified_clusters
    plt.scatter(data_with_clusters[xaxis], data_with_clusters[yaxis], c=data_with_clusters['Clusters'], cmap='rainbow')
    plt.xlabel(xaxis, fontsize=20)
    plt.ylabel(yaxis, fontsize=20)
    for i in data_with_clusters.values:
        plt.annotate(i[0][0:10], (i[chosen_array[0]], i[chosen_array[1]]), size=7)
    plt.savefig("Cluster - " + column_list[chosen_array[0]] + " - " + column_list[chosen_array[1]], bbox_inches='tight')
    fig = plt.figure()
    plt.figure().clear()
    plt.close()
    plt.cla()
    plt.clf()
scaler = preprocessing.MinMaxScaler()

data = pd.read_csv('data-outliers-removed.csv')
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

version_count_list = scaler.fit_transform(np.array(version_count_list).reshape(-1, 1)).tolist()
all_version_test_over_all_list = scaler.fit_transform(np.array(all_version_test_over_all_list).reshape(-1, 1)).tolist()
all_version_only_test_over_all_list = scaler.fit_transform(
    np.array(all_version_only_test_over_all_list).reshape(-1, 1)).tolist()
all_version_only_prod_over_all_list = scaler.fit_transform(
    np.array(all_version_only_prod_over_all_list).reshape(-1, 1)).tolist()
all_version_prod_and_test_over_all_list = scaler.fit_transform(
    np.array(all_version_prod_and_test_over_all_list).reshape(-1, 1)).tolist()
major_minor_version_count_list = scaler.fit_transform(np.array(major_minor_version_count_list).reshape(-1, 1)).tolist()
file_change_count_list = scaler.fit_transform(np.array(file_change_count_list).reshape(-1, 1)).tolist()
eloc_list = scaler.fit_transform(np.array(eloc_list).reshape(-1, 1)).tolist()
tloc_list = scaler.fit_transform(np.array(tloc_list).reshape(-1, 1)).tolist()
major_minor_test_over_all_list = scaler.fit_transform(np.array(major_minor_test_over_all_list).reshape(-1, 1)).tolist()
major_minor_prod_over_all_list = scaler.fit_transform(np.array(major_minor_prod_over_all_list).reshape(-1, 1)).tolist()

df = pd.DataFrame({
    'group': project_name_list,
    column_version_count: version_count_list,
    column_all_version_test_all: all_version_test_over_all_list,
    column_all_version_only_test_all: all_version_only_test_over_all_list,
    column_all_version_only_source_all: all_version_only_prod_over_all_list,
    column_all_version_source_and_test_all: all_version_prod_and_test_over_all_list,
    column_major_minor_version_count: major_minor_version_count_list,
    column_file_change_count: file_change_count_list,
    column_sloc: eloc_list,
    column_tloc: tloc_list,
    column_major_minor_test_all: major_minor_test_over_all_list,
    column_major_minor_prod_all: major_minor_prod_over_all_list
})

"""for i, project in enumerate(data.values):
    df.values[i][0] = project[0]
    df.values[i][1] = project[1]
    df.values[i][2] = project[2]
    df.values[i][3] = project[3]
    df.values[i][4] = project[4]
    df.values[i][5] = project[5]
    df.values[i][6] = project[6]
    df.values[i][7] = project[7]
    df.values[i][8] = project[8]
    df.values[i][9] = project[9]
    df.values[i][10] = project[10]
    df.values[i][11] = project[11]"""

