import Orange
import sys

data = Orange.data.Table("genestrain")
data2 = Orange.data.Table("genesblind")
learner = Orange.classification.bayes.NaiveLearner()
classifier = learner(data)
for d in data2:
	print(classifier(d))
