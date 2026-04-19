import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

export default defineConfig({
    site: 'https://isolgpus.github.io',
    base: '/Luxis',
    integrations: [
        starlight({
            title: 'Luxis',
            description: 'A type-safe, functional web framework built on Vert.x for Java 21.',
            social: [
                { icon: 'github', label: 'GitHub', href: 'https://github.com/isolgpus/Luxis' },
            ],
            editLink: {
                baseUrl: 'https://github.com/isolgpus/Luxis/edit/master/docs/',
            },
            sidebar: [
                {
                    label: 'Introduction',
                    items: [
                        { label: 'Why Luxis?', slug: 'why-luxis' },
                        { label: 'Getting Started', slug: 'getting-started' },
                    ],
                },
                {
                    label: 'Guides',
                    items: [
                        { label: 'Handler Pipeline', slug: 'guides/handler-pipeline' },
                        { label: 'HTTP Context', slug: 'guides/http-context' },
                        { label: 'Validation', slug: 'guides/validation' },
                        { label: 'Filters', slug: 'guides/filters' },
                        { label: 'Error Handling', slug: 'guides/error-handling' },
                        { label: 'Transactions', slug: 'guides/transactions' },
                        { label: 'File Upload & Download', slug: 'guides/file-upload-download' },
                        { label: 'WebSockets', slug: 'guides/websockets' },
                        { label: 'HTTP Client', slug: 'guides/http-client' },
                        { label: 'WebSocket Client', slug: 'guides/websocket-client' },
                        { label: 'Configuration', slug: 'guides/configuration' },
                        { label: 'Claude Code Skill', slug: 'guides/claude-code-skill' },
                    ],
                },
                {
                    label: 'Testing',
                    items: [
                        { label: 'Testing Your Handlers', slug: 'testing' },
                    ],
                },
            ],
        }),
    ],
});
