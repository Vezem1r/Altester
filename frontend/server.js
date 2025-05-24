import { createServer } from 'http';
import { readFileSync, existsSync } from 'fs';
import { join, extname } from 'path';

const port = process.env.PORT;

const server = createServer((req, res) => {
    const filePath = join('dist', req.url === '/' ? 'index.html' : req.url);

    if (existsSync(filePath)) {
        const ext = extname(filePath);
        const mimeTypes = {
            '.html': 'text/html',
            '.js': 'application/javascript',
            '.css': 'text/css',
            '.json': 'application/json'
        };

        res.writeHead(200, { 'Content-Type': mimeTypes[ext] || 'text/plain' });
        res.end(readFileSync(filePath));
    } else {
        // SPA fallback
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.end(readFileSync('dist/index.html'));
    }
});

server.listen(port, () => console.log(`Server running on port ${port}`));