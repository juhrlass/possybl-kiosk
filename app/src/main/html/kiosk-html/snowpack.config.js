/** @type {import("snowpack").SnowpackUserConfig } */
module.exports = {
  mount: {
    public: {url: "/", static: true, resolve: true},
    src:  "/dist"
  },
  buildOptions: {
    baseUrl: '/assets/',
    out: '../../assets',
    metaUrlPath: './snowpack'
  },
  plugins: [
    "@snowpack/plugin-postcss"
  ]
}
