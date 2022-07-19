package fr.netfit.demo.model;

import java.util.List;

public record Response(String numAccessCode, List<Account> accounts) {
}
