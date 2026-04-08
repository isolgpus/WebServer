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
                        { label: 'Error Handling', slug: 'guides/error-handling' },
                        { label: 'HTTP Context', slug: 'guides/http-context' },
                        { label: 'Validation', slug: 'guides/validation' },
                        { label: 'Filters', slug: 'guides/filters' },
                        { label: 'WebSockets', slug: 'guides/websockets' },
                        { label: 'HTTP Client', slug: 'guides/http-client' },
                        { label: 'File Upload & Download', slug: 'guides/file-upload-download' },
                        { label: 'Configuration', slug: 'guides/configuration' },
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
