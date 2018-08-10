/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

public class WoWPlayer {
    
    private String name = "";
    private long healCount = 0;
    private long damageCount = 0;
    
    // Get functions
    public String getName(){
        return name;
    }
    
    public long getHealCount(){
        return healCount;
    }
    
    public long getDamageCount(){
        return damageCount;
    }
    
   // Set Functions
   public void setName(String n){
        name = n;
    }
    
    public void setHealCount(long h){
        healCount = h;
    }
    
    public void setDamageCount(long d){
        damageCount = d;
    }
    
    // Functions to help with incremental increases
     public void increaseHealCount(long h){
        healCount = healCount + h;
    }
    
    public void increaseDamageCount(long d){
        damageCount = damageCount + d;
    }
    
       
}
