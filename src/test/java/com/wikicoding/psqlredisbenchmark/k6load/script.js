import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    stages: [
        { duration: '5s', target: 20 }, // ramp up
        { duration: '15s', target: 20 }, // stable
        { duration: '5s', target: 0 }, // ramp down to users
    ]
};

export default () => {
    const res = http.get('http://192.168.1.70:8080/');
    check(res, { '200': (r) => r.status === 200 });
    sleep(1);
};

// runs with: cat script.js | docker run --rm -i grafana/k6 run -