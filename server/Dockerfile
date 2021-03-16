FROM keymetrics/pm2:latest-alpine

# Bundle APP files
COPY GPSHistory.js .
#COPY package.json .
COPY pm2.json .
COPY data data/
COPY release release/

# Install app dependencies
ENV NPM_CONFIG_LOGLEVEL warn
#RUN npm install --production
RUN npm install express querystring node-cache sqlite3 moment body-parser -y

# Show current folder structure in logs
#RUN ls -al -R
EXPOSE 8095
CMD [ "pm2-runtime", "start", "pm2.json"]