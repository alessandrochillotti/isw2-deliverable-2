package it.uniroma2.alessandrochillotti.isw2.deliverable_2.utils;

import java.time.LocalDateTime;

public class ClassFile {
	private String fullName;
	private LocalDateTime creationDate;
	
	public ClassFile(String fullName) {
		this.fullName = fullName;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public LocalDateTime getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}
	
	@Override
    public boolean equals(Object file){
        if(file instanceof ClassFile){
            ClassFile toCompare = (ClassFile) file;
            return ((ClassFile) file).getFullName().equals(toCompare.getFullName());
        }
        return false;
    }
	
	@Override
    public int hashCode(){
        return 1;
    }
}
