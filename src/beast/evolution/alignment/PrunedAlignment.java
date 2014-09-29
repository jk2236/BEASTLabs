package beast.evolution.alignment;

import beast.app.util.Arguments;
import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.evolution.datatype.DataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Description("Sub-alignment generated by pruning some taxa and/or some sites. Can automatically detect taxa " +
        "with non-informative data (all sites are ambiguous).")
public class PrunedAlignment extends Alignment {
    public Input<Alignment> m_srcAlignment = new Input<>("source", "alignment to prune", Validate.REQUIRED);
    public Input<TaxonSet>  m_taxa = new Input<>("taxa",
            "taxa to prune (defaults to all non-informative taxa, i.e. data is all '???')");
    public Input<List<Integer>> m_sites = new Input<>("sites", "sites to retain (default all).");

  public PrunedAlignment() {}

  @Override
  public void initAndValidate() throws Exception {
      final Alignment source = m_srcAlignment.get();

      final DataType.Base udp = source.userDataTypeInput.get();
      if( udp != null ) {
          userDataTypeInput = source.userDataTypeInput;
      } else {
          dataTypeInput = source.dataTypeInput;
      }
      stateCountInput = source.stateCountInput;

      List<Integer> sites = m_sites.get();

      List<Sequence> sourceSeqs = source.sequenceInput.get();
      final TaxonSet taxonSet = m_taxa.get();

      if( sourceSeqs == null || sourceSeqs.size() == 0 ) {
          // This is truly ugly: alignment object like AlignmentFromTrait don't have sequences, and construct
          // the internals directly. We follow suit here.

          m_dataType = source.m_dataType;
          counts = new ArrayList<>();
          stateCounts = new ArrayList<>();

          final List<String> srcTaxa = source.taxaNames;
          final List<List<Integer>> srcCounts = source.counts;
          if( taxonSet != null ) {
             for(int i = 0; i < source.taxaNames.size(); ++i) {

                 if( taxonSet.getTaxonIndex(srcTaxa.get(i)) < 0  ) {
                    counts.add(srcCounts.get(i));
                    stateCounts.add(source.stateCounts.get(i));
                    taxaNames.add(srcTaxa.get(i));
                }
             }
          } else {
              for(int i = 0; i < source.counts.size(); ++i) {
                  final List<Integer> c = srcCounts.get(i);
                  for( Integer nc : c ) {
                      //assert( c.size() == 1 );
                      //final Integer nc = c.get(0);
                      if( m_dataType.getStatesForCode(nc).length != m_dataType.getStateCount() ) {
                          counts.add(c);
                          stateCounts.add(source.stateCounts.get(i));
                          taxaNames.add(srcTaxa.get(i));
                          break;
                      }
                  }
              }
          }

          if( sites != null ) {
              for(int i = 0; i < taxaNames.size(); ++i) {
                  List<Integer> newCounts = new ArrayList<>();
                  List<Integer> c = counts.get(i);
                  for( int s : sites ) {
                      newCounts.add( c.get(s) );
                  }
                  counts.set(i, newCounts);
              }
          }

          calcPatterns();
          return;
      }

      if( sites == null ) {
          final int nSites = source.getSiteCount();
          sites = new ArrayList<Integer>(nSites);
          for (int k = 0; k < nSites; ++k) {
              sites.add(k);
          }
      }

      List<Sequence> seqs = new ArrayList<>();

      if( taxonSet != null ) {
          for (Sequence seq :  sourceSeqs ) {
            if( taxonSet.getTaxonIndex(seq.taxonInput.get()) < 0) {
              seqs.add(seq);
            }
         }
      } else {
          for (Sequence seq : sourceSeqs) {
              List<Integer> states = seq.getSequence(source.m_dataType);
              final int sn = source.m_dataType.getStateCount();
              boolean hasData = false;
              for (int i : sites) {
                  if( states.get(i) >= 0 && states.get(i) < sn ) {
                      hasData = true;
                      break;
                  }
              }
              if( hasData ) {
                  seqs.add(seq);
              }
          }
      }

      if( m_sites.get() != null ) {
          for(int k = 0; k < seqs.size(); ++k ) {
              Sequence seq = seqs.get(k);
              List<Integer> states = seq.getSequence(source.m_dataType);
              StringBuilder s = new StringBuilder();
              for (int i : sites) {
                  s.append(states.get(i));
                  s.append(',');
              }
              s.deleteCharAt(s.length() - 1);
              seqs.set(k, new Sequence(seq.taxonInput.get(), s.toString()));
          }
      }
      setInputValue("sequence", seqs);
      super.initAndValidate();
  }
}
