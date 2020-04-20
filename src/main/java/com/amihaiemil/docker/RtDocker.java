/**
 * Copyright (c) 2018-2019, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1)Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3)Neither the name of docker-java-api nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.amihaiemil.docker;

import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/**
 * Restful Docker.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
abstract class RtDocker implements Docker {

    /**
     * Apache HttpClient which sends the requests.
     */
    private final HttpClient client;

    /**
     * Base URI.
     */
    private final URI baseUri;
    
    /**
     * Ctor.
     * @param client Given HTTP Client.
     * @param baseUri Base URI.
     */
    RtDocker(final HttpClient client, final URI baseUri) {
        this.client = client;
        this.baseUri = baseUri;
    }
    
    @Override
    public final boolean ping() throws IOException {
        final HttpGet ping = new HttpGet(this.baseUri.toString() + "/_ping");
        final HttpResponse response = this.client.execute(ping);
        ping.releaseConnection();
        return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }

    @Override
    public Events events() {
        return new RtEvents(
            this.client,
            URI.create(this.baseUri.toString() + "/events"),
            this
        );
    }

    @Override
    public final Containers containers() {
        return new ListedContainers(
            this.client,
            URI.create(this.baseUri.toString() + "/containers"),
            this
        );
    }

    @Override
    public final Images images() {
        return new ListedImages(
            this.client,
            URI.create(this.baseUri.toString() + "/images"),
            this
        );
    }

    @Override
    public final Networks networks() {
        return new ListedNetworks(
            this.client,
            URI.create(this.baseUri.toString() + "/networks"),
            this
        );
    }

    @Override
    public final Volumes volumes() {
        return new ListedVolumes(
            this.client,
            URI.create(this.baseUri.toString() + "/volumes"),
            this
        );
    }

    @Override
    public final Execs execs() {
        return new RtExecs(
            this.client,
            URI.create(this.baseUri.toString() + "/exec"),
            this
        );
    }

    @Override
    public final Swarm swarm() {
        return new RtSwarm(
            this.client,
            URI.create(this.baseUri.toString().concat("/swarm")), 
            this
        );
    }

    @Override
    public DockerSystem system() {
        return new RtDockerSystem(
            this.client,
            URI.create(this.baseUri.toString().concat("/system")),
            this
        );
    }


    @Override
    public Plugins plugins() {
        throw new UnsupportedOperationException(
            String.join(" ",
                "Plugins API is not yet implemented.",
                "If you can contribute please",
                "do it here: https://www.github.com/amihaiemil/docker-java-api"
            )
        );
    }

    @Override
    public Version version() throws IOException {
        final String versionUri = this.baseUri.toString() + "/version";
        return new RtVersion(
            this.client,
            URI.create(versionUri)
        );
    }

    @Override
    public Info info() throws IOException {
        final HttpGet info = new HttpGet(this.baseUri.toString() + "/info");
        try {
            return new RtInfo(
                this.client.execute(
                    info,
                    new ReadJsonObject(
                        new MatchStatus(info.getURI(), HttpStatus.SC_OK)
                    )
                ),
                this
            );
        } finally {
            info.releaseConnection();
        }
    }

    @Override
    public HttpClient httpClient() {
        return this.client;
    }
}
