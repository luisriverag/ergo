package org.ergoplatform.local

import org.ergoplatform.modifiers.history.header.Header
import org.ergoplatform.modifiers.history.popow.NipopowProof
import scorex.util.ModifierId

sealed trait NipopowProofVerificationResult

case object BetterChain extends NipopowProofVerificationResult

case object NoBetterChain extends NipopowProofVerificationResult

case object ValidationError extends NipopowProofVerificationResult

case object WrongGenesis extends NipopowProofVerificationResult

/**
  * A verifier for PoPoW proofs. During its lifetime, it processes many proofs with the aim of deducing at any given
  * point what is the best (sub)chain rooted at the specified genesis.
  *
  * @param genesisId    - the block id of the genesis block
  */
class NipopowVerifier(genesisId: ModifierId) {

  var bestProofOpt: Option[NipopowProof] = None

  def bestChain: Seq[Header] = {
    bestProofOpt.map(_.headersChain).getOrElse(Seq())
  }

  /**
    * @return - if newProof is replacing older ones
    */
  def process(newProof: NipopowProof): NipopowProofVerificationResult = {
    if (newProof.headersChain.head.id == genesisId) {
      bestProofOpt match {
        case Some(bestProof) =>
          if (newProof.isBetterThan(bestProof)) {
            bestProofOpt = Some(newProof)
            BetterChain
          } else {
            NoBetterChain
          }
        case None =>
          if (newProof.isValid) {
            bestProofOpt = Some(newProof)
            BetterChain
          } else {
            ValidationError
          }
      }
    } else {
      WrongGenesis
    }
  }

}
