package model;

import ec.gp.GPData;

public class PDPData extends GPData {

    private static final long serialVersionUID = 1236137301060685291L;

    protected boolean result;
    protected Instance instance;




    public PDPData(){
        result = false;
        instance = new Instance();
    }
    @Override
    public PDPData clone() { //se crea una nueva variable PDPData con el mismo contenido, un CLON
        PDPData clon = new PDPData();
        clon.result = this.result; 			//mismo result
        clon.instance = instance.clone();	//misma instancia
        return clon;
    }

    //GETTERS
    public boolean			getResult()				{	return result;		}
    public Instance			getInstance() 			{	return instance;	}
    public PDPInstance      getPDPInstance()        {   return instance.getPdpInstance();       }

    //SETTERS
    public void 			setResult(boolean cond)				{	this.result = cond;		}
    public void 			setInstance(final Instance inst)	{	this.instance = (inst);	}
    public void             setPDPInstance(PDPInstance pdp)     {   this.instance.setPdpInstance(pdp);      }



    //public String toString() {return ("[result=" + result + "]\n[instance=" + instance.toString(false) +"]\n[knapsack=" + instance.getIndependenSet().toString(false));}

}