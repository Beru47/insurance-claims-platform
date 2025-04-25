package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Client;
import com.vermeg.sinistpro.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client createClient(Client client) {
        if (clientRepository.findByCin(client.getCin()).isPresent()) {
            throw new IllegalArgumentException("Client with CIN " + client.getCin() + " already exists");
        }
        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }
}