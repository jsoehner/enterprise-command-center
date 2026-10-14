export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'header-max-length': [2, 'always', 150],
    'body-max-line-length': [0, 'always'],
    'footer-max-line-length': [0, 'always']
  },
  defaultIgnores: true,
  ignores: [
    (commit) => /^(Merge\s|Revert\s|Test\/|feat\/|fix\/|chore\/)/i.test(commit)
  ]
};
