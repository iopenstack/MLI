package mli.ml.classification

import mli.interface._
import mli.ml._
import spark.mllib.classification.SVMLocalRandomSGD

class SVMModel(
    trainingTbl: MLTable,
    trainingParams: SVMParameters,
    trainingTime: Long,
    val model: spark.mllib.classification.SVMModel)
  extends Model[SVMParameters](trainingTbl, trainingTime, trainingParams) {


  /* Predicts the label of a given data point. */
  def predict(x: MLRow) : MLValue = {
    MLValue(model.predict(x.toDoubleArray))
  }

  /**
   * Provides a user-friendly explanation of this model.
   * For example, plots or console output.
   */
  def explain() : String = {
    "Weights: " + model.weights
  }
}

case class SVMParameters(
    targetCol: Int = 0,
    learningRate: Double = 0.2,
    regParam: Double = 0.0,
    maxIterations: Int = 100,
    minLossDelta: Double = 1e-5,
    minibatchFraction: Double = 1.0,
    optimizer: String = "SGD")
  extends AlgorithmParameters


object SVMAlgorithm extends Algorithm[SVMParameters] with Serializable {

  def defaultParameters() = SVMParameters()

  def train(data: MLTable, params: SVMParameters): SVMModel = {

    // Initialization
    assert(data.numRows > 0)
    assert(data.numCols > 1)

    val startTime = System.currentTimeMillis

    //Run gradient descent on the data.
    val weights = SVMLocalRandomSGD.train(
      data.toRDD(params.targetCol).map(r => (r._1.toInt, r._2)),
      params.maxIterations,
      params.learningRate,
      params.regParam,
      params.minibatchFraction)

    val trainTime = System.currentTimeMillis - startTime

    new SVMModel(data, params, trainTime, weights)
  }

}





