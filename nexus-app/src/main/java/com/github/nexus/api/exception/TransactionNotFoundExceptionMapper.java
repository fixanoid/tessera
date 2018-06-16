package com.github.nexus.api.exception;

import com.github.nexus.transaction.exception.TransactionNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class TransactionNotFoundExceptionMapper implements ExceptionMapper<TransactionNotFoundException> {

    private static final Logger LOGGER = Logger.getLogger(TransactionNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(TransactionNotFoundException e) {
        LOGGER.log(Level.SEVERE, "",e);

        return Response.status(Response.Status.BAD_REQUEST)
            .entity(e.getMessage())
            .header("Content-Type", MediaType.TEXT_PLAIN)
            .build();
    }
}