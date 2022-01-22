module.exports = {
    purge: [
     './public/**/*.html',
     './src/**/*.js',
    ],
    darkMode: false, // or 'media' or 'class'
    theme: {
      extend: {
        screens: {
            sm: "600px"
        },
      },
      minWidth: {
        '7/10': '70%'
      }
    },
    variants: {
        extend: {
          outline: ["focus"]
        }
    },
    plugins: [
        require('@tailwindcss/custom-forms')
    ],
}