package org.alicebot.ab;
public class Clause {

  private String subj;

  private String pred;

  private String obj;

  private Boolean affirm;

    public Clause(String s, String p, String o) {
        this(s, p, o, true);
    }

    public Clause(String s, String p, String o, Boolean affirm) {
        subj = s;
        pred = p;
        obj = o;
        this.affirm = affirm;
    }
    public Clause(Clause clause) {
        this(clause.subj, clause.pred, clause.obj, clause.affirm);
  }

  public String getSubj() {
    return subj;
  }

  public void setSubj(final String subj) {
    this.subj = subj;
  }

  public Boolean getAffirm() {
    return affirm;
  }

  public String getPred() {
    return pred;
  }

  public void setPred(final String pred) {
    this.pred = pred;
  }

  public String getObj() {
    return obj;
  }

  public void setObj(final String obj) {
    this.obj = obj;
    }
}
