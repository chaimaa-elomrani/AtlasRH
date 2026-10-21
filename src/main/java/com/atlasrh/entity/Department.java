package com.atlasrh.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "department" , uniqueConstraints = @UniqueConstraint( // unique contrainte est une list de contraintes et @uni... est une de ces containtes la
		name = "uk_departments_name" , // contrainte dunicité : uk --> unique key
		columnNames = "name"
))

public class Department{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;

	@Column(nullable = false , length = 100)
	private String name ;

	@Column(length = 500)
	private String description ;

	protected Department(){

	}

	public Department(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
