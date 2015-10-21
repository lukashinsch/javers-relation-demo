package com.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Parent {
	@Id
	@GeneratedValue
	private Long id;

	private String name;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Child> children;
}
